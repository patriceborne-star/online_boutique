![Online Boutique Home Page](<70 - Home page image.png>)

# Online Boutique on yugabyteDB

This project is a Spring Boot version of the Online Boutique demo, adapted to run on yugabyteDB with a simple local setup script and a seeded product catalog and user list.

## What This Project Does

- Serves a storefront with a nine-product catalog.
- Uses yugabyteDB tables for users, products, carts, orders, and order items.
- Supports login, cart management, checkout, profile updates, and order history.
- Advances order status automatically from `pending` to `shipped` to `delivered` on a schedule.

## Technology Stack

- Java 21
- Spring Boot 3.2.5
- Thymeleaf
- Spring JDBC
- PostgreSQL JDBC driver
- yugabyteDB
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

The schema is intentionally straightforward so it is easy to inspect in yugabyteDB and easy to extend for demos or workshops.

## Seeded Demo Users

The current seed data includes these demo accounts, all using the password `password`:

- Daniel Farrell
- Alan Caldera
- Jim Knicely
- Prasad Radhakrishnan
- Susan Flynn

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
- [`OrderHistoryController.java`](<online-boutique/src/main/java/com/yugabyte/boutique/controller/OrderHistoryController.java>)

### Scheduled Order Progression

Order status changes are handled by a scheduler that runs every 60 seconds:

- [`OrderStatusScheduler.java`](<online-boutique/src/main/java/com/yugabyte/boutique/service/OrderStatusScheduler.java>)

Status behavior:

- `pending` to `shipped` after 10 minutes
- `shipped` to `delivered` after another 10 minutes

## How To Run

1. Update [`properties.ini`](<properties.ini>) if your yugabyteDB host, port, database name, user, or application port differ from the defaults.
2. Run [`60 - Run web app.sh`](<60 - Run web app.sh>).
3. Open the application in your browser on the configured port.
4. Sign in at `/login` with one of the seeded demo users.

The runner script does the following automatically:

1. Drops and recreates the target database.
2. Loads the schema from [`schema.sql`](<online-boutique/src/main/resources/schema.sql>).
3. Loads the seed data from [`data.sql`](<online-boutique/src/main/resources/data.sql>).
4. Builds the application jar if needed.
5. Launches the Spring Boot app.

## Notes

- This project is optimized for local demos and learning rather than production deployment.
- The database initialization is handled by the shell script, not by Spring Boot startup.
- Built artifacts are currently present under `online-boutique/target`, but the source of truth is under `online-boutique/src`.

## Main Entry Points

- [`online-boutique/pom.xml`](<online-boutique/pom.xml>)
- [`online-boutique/src/main/java/com/yugabyte/boutique/Application.java`](<online-boutique/src/main/java/com/yugabyte/boutique/Application.java>)
- [`online-boutique/src/main/resources/application.properties`](<online-boutique/src/main/resources/application.properties>)

