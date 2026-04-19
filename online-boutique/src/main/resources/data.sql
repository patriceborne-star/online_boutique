-- Seed product catalog (9 products from the original Online Boutique)

INSERT INTO products (id, name, description, picture, currency_code, price_units, price_nanos, categories)
VALUES
('OLJCESPC7Z', 'Sunglasses',
 'Add a modern touch to your outfits with these sleek aviator sunglasses.',
 '/static/products/sunglasses.jpg', 'USD', 19, 990000000, 'accessories'),

('66VCHSJNUP', 'Tank Top',
 'Perfectly cropped cotton tank, with a scooped neckline.',
 '/static/products/tank-top.jpg', 'USD', 18, 990000000, 'clothing,tops'),

('1YMWWN1N4O', 'Watch',
 'This gold-tone stainless steel watch will work with most of your outfits.',
 '/static/products/watch.jpg', 'USD', 109, 990000000, 'accessories'),

('L9ECAV7KIM', 'Loafers',
 'A neat addition to your summer wardrobe.',
 '/static/products/loafers.jpg', 'USD', 89, 990000000, 'footwear'),

('2ZYFJ3GM2N', 'Hairdryer',
 'This lightweight hairdryer has 3 heat and speed settings. It''s perfect for travel.',
 '/static/products/hairdryer.jpg', 'USD', 24, 990000000, 'hair,beauty'),

('0PUK6V6EV0', 'Candle Holder',
 'This small but intricate candle holder is an excellent gift.',
 '/static/products/candle-holder.jpg', 'USD', 18, 990000000, 'decor,home'),

('LS4PSXUNUM', 'Salt & Pepper Shakers',
 'Add some flavor to your kitchen.',
 '/static/products/salt-and-pepper-shakers.jpg', 'USD', 18, 490000000, 'kitchen'),

('9SIQT8TOJO', 'Bamboo Glass Jar',
 'This bamboo glass jar can hold 57 oz (1.7 l) and is perfect for any kitchen.',
 '/static/products/bamboo-glass-jar.jpg', 'USD', 5, 490000000, 'kitchen'),

('6E92ZMYYFZ', 'Mug',
 'A simple mug with a mustard interior.',
 '/static/products/mug.jpg', 'USD', 8, 990000000, 'kitchen')

ON CONFLICT (id) DO NOTHING;

-- Seed users

INSERT INTO users (email, password, first_name, last_name, street_address, city, state, zip_code, country, phone,
                   credit_card_number, credit_card_exp_month, credit_card_exp_year, credit_card_cvv)
VALUES
('daniel.farrell@ybmail.com', 'password', 'Daniel', 'Farrell',
 '1 Infinite Loop', 'Cupertino', 'CA', '95014', 'United States',
 '408-555-3456', '4916338506082832', 12, 2028, '321'),

('alan.caldera@ybmail.com', 'password', 'Alan', 'Caldera',
 '350 Fifth Avenue', 'New York', 'NY', '10118', 'United States',
 '212-555-5678', '5425233430109903', 3, 2027, '456'),

('jim.knicely@ybmail.com', 'password', 'Jim', 'Knicely',
 '233 S Wacker Drive', 'Chicago', 'IL', '60606', 'United States',
 '312-555-9012', '6011111111111117', 6, 2029, '789'),

('prasad.radhakrishnan@ybmail.com', 'password', 'Prasad', 'Radhakrishnan',
 '1600 Amphitheatre Parkway', 'San Ramon', 'CA', '94043', 'United States',
 '650-555-1234', '4432801561520454', 1, 2028, '672'),

('susan.flynn@ybmail.com', 'password', 'Susan', 'Flynn',
 '1200 Getty Center Drive', 'Los Angeles', 'CA', '90049', 'United States',
 '310-555-7890', '4539578763621486', 9, 2027, '654')

ON CONFLICT (email) DO NOTHING;
