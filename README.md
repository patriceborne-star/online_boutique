![Online Boutique Home Page](<70 - Home page image.png>)

# Online Boutique on Oracle

This project is a Spring Boot version of the Online Boutique demo, adapted to run on Oracle with a simple local setup script and a seeded product catalog and user list.

## What This Project Does

- Serves a storefront with a nine-product catalog.
- Uses Oracle tables for users, products, carts, orders, and order items.
- Supports login, cart management, checkout, profile updates, and order history.
- Advances order status automatically from `pending` to `shipped` to `delivered` on a schedule.

## Technology Stack

- Java 21
- Spring Boot 3.2.5
- Thymeleaf
- Spring JDBC
- Oracle JDBC driver
- Oracle
- Maven wrapper

## Project Layout

- [`60 - Run web app.sh`](<60 - Run web app.sh>) recreates the database, loads the schema and seed data, and starts the web app.
- [`properties.ini`](<properties.ini>) contains the database connection and application port settings.
- [`online-boutique/src/main/resources/schema.sql`](<online-boutique/src/main/resources/schema.sql>) defines the database schema.
- [`online-boutique/src/main/resources/data.sql`](<online-boutique/src/main/resources/data.sql>) seeds the product catalog and demo users.
- [`online-boutique/src/main/java/com/yugabyte/boutique`](<online-boutique/src/main/java/com/yugabyte/boutique>) contains the Spring Boot application, controllers, repositories, and services.
- [`online-boutique/src/main/resources/templates`](<online-boutique/src/main/resources/templates>) contains the Thymeleaf pages.

## Database Model

The application uses these primary tables:

- `users`
- `products`
- `cart_items`
- `orders`
- `order_items`

The schema is intentionally straightforward so it is easy to inspect in Oracle and easy to extend for demos or workshops.

## Seeded Demo Users

The current seed data includes these demo accounts, all using the password `password`:

- Daniel Farrell
- Alan Caldera
- Jim Knicely
- Prasad Radhakrishnan
- Susan Flynn
- Patrice Borne

Each user is stored with a `@ybmail.com` email address in [`data.sql`](<online-boutique/src/main/resources/data.sql>).

## Key Application Flows

### Sign In

The login page loads users from the database and lets you sign in with a seeded account:

- [`AuthController.java`](<online-boutique/src/main/java/com/yugabyte/boutique/controller/AuthController.java>)
- [`login.html`](<online-boutique/src/main/resources/templates/login.html>)

### Browse and Shop

The home page lists products and converts prices using the selected currency:

- [`HomeController.java`](<online-boutique/src/main/java/com/yugabyte/boutique/controller/HomeController.java>)

### Checkout

Checkout places an order, stores shipping details, and renders an order confirmation page:

- [`CheckoutController.java`](<online-boutique/src/main/java/com/yugabyte/boutique/controller/CheckoutController.java>)

### Profile and Order History

Users can update their profile and review prior orders:

- [`ProfileController.java`](<online-boutique/src/main/java/com/yugabyte/boutique/controller/ProfileController.java>)
