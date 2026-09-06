CREATE TABLE restaurant_order_lines (
    order_id UUID NOT NULL REFERENCES restaurant_orders(id),
    line_no INTEGER NOT NULL CHECK (line_no >= 0),
    sku VARCHAR(120) NOT NULL,
    modifier VARCHAR(120),
    quantity INTEGER NOT NULL CHECK (quantity BETWEEN 1 AND 99),
    unit_price_minor BIGINT NOT NULL CHECK (unit_price_minor >= 0),
    PRIMARY KEY (order_id, line_no)
);
