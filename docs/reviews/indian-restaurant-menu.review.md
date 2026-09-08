# Indian restaurant menu review draft

**Status:** Non-executable draft awaiting owner approval. No menu has been imported, published, priced for ordering, or sent to Railway, Toast, Twilio, or any provider.

## Source preservation

The complete pasted menu is preserved verbatim in the `source_text` field of `indian-restaurant-menu.review-draft.json`. Extracted records retain the displayed category, item name, price, and source block.

## Extraction summary

- Extracted displayed entries: **256**
- Entries with minimum (`+`) prices: **18**
- Entries with a repeated normalized item name: **10**
- Allergy/cross-contact facts verified: **0**
- Approved size/modifier choices: **0**

## Required owner review

- Confirm every fixed price, currency, tax treatment, and whether card-processing surcharges apply to phone orders.
- Define each `+` price as a concrete tray size, serving count, modifier, or approved price range before it can be quoted.
- Review same-name entries across categories, including featured versus main-menu items, for intentional duplication.
- Supply ingredient, allergen, cross-contact, dietary, spice, serving, availability, and preparation facts from an approved source. Descriptions alone cannot support allergy or dietary assurances.
- Supply valid size, quantity, add-on, removal, and substitution choices. The draft does not infer any.

## Detected price and duplication concerns

### Minimum prices

- TRAYS - VEG: **Hyderabad Veg Dum Biryani** — `$35.00+`; size/quantity rule missing.
- TRAYS - VEG: **Akshaya Patra Guthi Vankaya Biryani** — `$35.00+`; size/quantity rule missing.
- TRAYS - VEG: **Gobi Biryani** — `$40.00+`; size/quantity rule missing.
- TRAYS - VEG: **Akshaya Patra Special Paneer Biryani** — `$45.00+`; size/quantity rule missing.
- TRAYS - VEG: **Chef Special Paneer Biryani** — `$45.00+`; size/quantity rule missing.
- TRAYS - VEG: **Paneer Ghee Roast Biryani** — `$45.00+`; size/quantity rule missing.
- TRAYS - VEG: **Kofta Veg Biryani** — `$45.00+`; size/quantity rule missing.
- TRAYS - NON VEG: **Egg Biryani** — `$35.00+`; size/quantity rule missing.
- TRAYS - NON VEG: **Egg Roast Biryani** — `$35.00+`; size/quantity rule missing.
- TRAYS - NON VEG: **Hyderabad Chicken Dum Biryani** — `$40.00+`; size/quantity rule missing.
- TRAYS - NON VEG: **Hyderabad Goat Dum Biryani** — `$50.00+`; size/quantity rule missing.
- TRAYS - NON VEG: **Akshaya Patra Special Chicken Biryani** — `$45.00+`; size/quantity rule missing.
- TRAYS - NON VEG: **Chicken Ghee Roast Biryani** — `$45.00+`; size/quantity rule missing.
- TRAYS - NON VEG: **Chef Special Chicken Biryani** — `$45.00+`; size/quantity rule missing.
- TRAYS - NON VEG: **Mutton Ghee Roast Biryani** — `$50.00+`; size/quantity rule missing.
- TRAYS - NON VEG: **Kheema Goat Biryani** — `$50.00+`; size/quantity rule missing.
- TRAYS - NON VEG: **Fish Supreme Biryani** — `$50.00+`; size/quantity rule missing.
- TRAYS - NON VEG: **Shrimp Roast Biryani** — `$60.00+`; size/quantity rule missing.

### Potential price-label anomalies

- **1/2 Tomato Soup*** is listed at `$7.99`, while **Tomato Soup*** is listed at `$5.99`. Confirm the labels, serving sizes, and prices.
- **1/2 Goat Soup** is listed at `$11.99`, while **Special Goat Soup** is listed at `$9.99`. Confirm whether these are different servings or a price-label reversal.
- The stated **3.00% card-processing surcharge** has no approved phone-order applicability or rounding rule. Do not include it in a quote until the owner approves the policy.

### Repeated normalized names

- #1 Featured Items: Chicken Supreme ($14.99), #60 NON VEG APPETIZERS: Chicken Supreme ($14.99)
- #2 Featured Items: Garlic Naan ($3.99), #204 BREADS: Garlic Naan ($3.99)
- #3 Featured Items: Hyderabad Chicken Dum Biriyani ($14.99), #163 BIRYANIS - NON VEG: Hyderabad Chicken Dum Biriyani ($14.99)
- #158 BIRYANIS - VEG: Chef Special Paneer Biryani ($14.99), #241 TRAYS - VEG: Chef Special Paneer Biryani ($45.00+)
- #166 BIRYANIS - NON VEG: Chef Special Chicken Biryani ($14.99), #250 TRAYS - NON VEG: Chef Special Chicken Biryani ($45.00+)

## Category inventory

- **Featured Items** — 3 displayed entries
- **SOUPS - VEG** — 2 displayed entries
- **SOUPS - NON VEG** — 2 displayed entries
- **TIFFINS AND DOSAS** — 17 displayed entries
- **VEG APPETIZERS** — 34 displayed entries
- **NON VEG APPETIZERS** — 31 displayed entries
- **TANDOORI - VEG** — 3 displayed entries
- **TANDOORI - NON VEG** — 8 displayed entries
- **CURRIES - VEG** — 19 displayed entries
- **CURRIES - NON VEG** — 31 displayed entries
- **BIRYANIS - VEG** — 8 displayed entries
- **BIRYANIS - NON VEG** — 15 displayed entries
- **INDO CHINESE - VEG** — 9 displayed entries
- **INDO CHINESE - NON VEG** — 12 displayed entries
- **KOTHU PAROTA** — 5 displayed entries
- **BREADS** — 7 displayed entries
- **KIDS MENU** — 3 displayed entries
- **DRINKS** — 12 displayed entries
- **DESSERTS** — 6 displayed entries
- **SPECIALS** — 9 displayed entries
- **TRAYS - VEG** — 7 displayed entries
- **TRAYS - NON VEG** — 11 displayed entries
- **THALI** — 2 displayed entries
