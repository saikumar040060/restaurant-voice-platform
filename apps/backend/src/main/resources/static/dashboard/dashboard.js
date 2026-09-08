(() => {
  const tokenKey = "harborvoice.session";
  const loginPanel = document.querySelector("#login-panel");
  const workspace = document.querySelector("#workspace");
  const content = document.querySelector("#content");
  const status = document.querySelector("#status");
  const identity = document.querySelector("#identity");
  const token = () => sessionStorage.getItem(tokenKey);
  const setStatus = value => { status.textContent = value; };
  const signedOut = () => { sessionStorage.removeItem(tokenKey); workspace.hidden = true; loginPanel.hidden = false; identity.textContent = "Sign in to view your tenant."; clear(); };
  const request = async path => {
    const response = await fetch(path, { headers: { Authorization: `Bearer ${token()}` }, cache: "no-store" });
    if (response.status === 401) { signedOut(); throw new Error("Your session expired. Please sign in again."); }
    if (!response.ok) throw new Error(response.status === 403 ? "You do not have access to this view." : "Request failed.");
    return response.json();
  };
  const clear = () => { content.replaceChildren(); };
  const table = (headers, rows) => {
    const element = document.createElement("table"); const head = document.createElement("tr");
    headers.forEach(value => { const th = document.createElement("th"); th.textContent = value; head.append(th); });
    const thead = document.createElement("thead"); thead.append(head); element.append(thead);
    const body = document.createElement("tbody"); rows.forEach(row => { const tr = document.createElement("tr"); row.forEach(value => { const td = document.createElement("td"); td.textContent = String(value ?? ""); tr.append(td); }); body.append(tr); });
    element.append(body); return element;
  };
  const menu = async () => { const data = await request("/api/v1/restaurant/menu"); clear(); const hours = document.createElement("p"); hours.textContent = `Fictional reference hours: ${data.hours.timezone} (shown for sandbox evaluation only).`; content.append(hours, table(["Item", "Price", "Available choices", "Required choices"], data.items.map(item => [item.name, `$${(item.priceMinor / 100).toFixed(2)}`, Object.keys(item.modifiers).join(", "), [...item.requiredModifierGroups].join(", ")]))); };
  const orders = async () => { const data = await request("/api/v1/restaurant/orders"); clear(); content.append(table(["Order", "State", "Total", "Created"], data.map(item => [item.orderId, item.state, `${item.currency} ${(item.totalMinor / 100).toFixed(2)}`, item.createdAt]))); };
  const operations = async () => { const data = await request("/api/v1/operations/snapshot"); clear(); content.append(table(["Provider", "State", "Observed"], Object.values(data.providers).map(item => [item.provider, item.state, item.observedAt]))); };
  const writeReviewDecision = async (item, decision, correction, known) => {
    const response = await fetch("/api/v1/restaurant/menu-review-draft/decisions", {
      method: "POST", headers: { Authorization: `Bearer ${token()}`, "Content-Type": "application/json" },
      body: JSON.stringify({ itemIndex: item.source_index, decision, correction, expectedVersion: known?.version || 0 })
    });
    if (response.status === 401) { signedOut(); throw new Error("Your session expired. Please sign in again."); }
    if (response.status === 409) throw new Error("This review item changed. Reload and try again.");
    if (!response.ok) throw new Error(response.status === 403 ? "You do not have owner review access." : "Decision could not be saved.");
    return response.json();
  };
  const review = async () => {
    const [data, saved] = await Promise.all([request("/api/v1/restaurant/menu-review-draft"), request("/api/v1/restaurant/menu-review-draft/decisions")]);
    const decisions = Object.fromEntries(saved.map(decision => [decision.itemIndex, decision])); clear();
    const flagged = data.items.filter(item => item.review_flags.length); const byCategory = Object.groupBy(flagged, item => item.category);
    const note = document.createElement("p"); note.textContent = "Owner review only. Decisions are tenant-scoped, versioned, audited, and remain unpublished. Import and publication are disabled."; content.append(note);
    const decide = async (item, decision, correction) => { try { await writeReviewDecision(item, decision, correction, decisions[item.source_index]); await review(); } catch (error) { setStatus(error.message); } };
    Object.entries(byCategory).forEach(([category, items]) => { const heading = document.createElement("h2"); heading.textContent = category; content.append(heading); const rows = items.map(item => [item.name, item.listed_price, item.review_flags.join(", "), decisions[item.source_index]?.decision || "PENDING"]); content.append(table(["Item", "Price", "Flags", "Unpublished decision"], rows)); items.forEach(item => { const controls = document.createElement("p"); [["approve", "APPROVED"], ["reject", "REJECTED"]].forEach(([label, decision]) => { const button = document.createElement("button"); button.textContent = `${label} ${item.name}`; button.addEventListener("click", () => decide(item, decision, null)); controls.append(button); }); const correct = document.createElement("button"); correct.textContent = `correct ${item.name}`; correct.addEventListener("click", () => { const correction = prompt("Owner correction (unpublished review only):"); if (correction) decide(item, "CORRECTED", correction); }); controls.append(correct); content.append(controls); }); });
    const bulk = document.createElement("button"); bulk.textContent = "Approve all currently flagged entries (unpublished)"; bulk.addEventListener("click", async () => { try { const response = await fetch("/api/v1/restaurant/menu-review-draft/decisions/bulk", { method: "POST", headers: { Authorization: `Bearer ${token()}`, "Content-Type": "application/json" }, body: JSON.stringify({ decisions: flagged.map(item => ({ itemIndex: item.source_index, decision: "APPROVED", correction: null, expectedVersion: decisions[item.source_index]?.version || 0 })) }) }); if (response.status === 401) { signedOut(); throw new Error("Your session expired. Please sign in again."); } if (response.status === 409) throw new Error("A review item changed. Nothing was saved; reload and try again."); if (!response.ok) throw new Error("Bulk decision could not be saved."); await review(); } catch (error) { setStatus(error.message); } }); content.prepend(bulk);
  };
  const transfers = async () => { const data = await request("/api/v1/operations/escalations"); clear(); content.append(table(["Case", "Reason", "State", "Created"], data.map(item => [item.caseId, item.reason, item.state, item.createdAt]))); };
  const show = async view => { try { setStatus("Loading…"); await ({ menu, orders, review, operations, transfers })[view](); setStatus(""); } catch (error) { clear(); setStatus(error.message); } };
  const refreshIdentity = async () => { const actor = await request("/api/v1/me"); identity.textContent = `Signed in as ${actor.role}`; loginPanel.hidden = true; workspace.hidden = false; show("menu"); };
  document.querySelector("#login-form").addEventListener("submit", async event => { event.preventDefault(); setStatus("Signing in…"); try { const response = await fetch("/api/v1/auth/login", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ username: document.querySelector("#username").value, password: document.querySelector("#password").value }) }); if (!response.ok) throw new Error("Sign-in failed."); sessionStorage.setItem(tokenKey, (await response.json()).token); await refreshIdentity(); setStatus(""); } catch (error) { setStatus(error.message); } });
  document.querySelector("nav").addEventListener("click", event => { if (event.target.dataset.view) show(event.target.dataset.view); });
  document.querySelector("#logout").addEventListener("click", async () => { try { await fetch("/api/v1/auth/logout", { method: "POST", headers: { Authorization: `Bearer ${token()}` } }); } finally { signedOut(); } });
  const reauthorize = () => { if (token()) refreshIdentity().catch(signedOut); };
  window.addEventListener("focus", reauthorize);
  setInterval(reauthorize, 60000);
  if (token()) refreshIdentity().catch(signedOut);
})();
