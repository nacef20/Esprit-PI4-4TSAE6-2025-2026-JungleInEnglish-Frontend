# Fix: "Packet for query is too large"

Your error: **Packet for query is too large (1,244,003 > 1,048,576)**  
The MySQL server limit is 1 MB; your payload (e.g. base64 image) is larger. You must **raise the server limit once on the MySQL server**.

---

## Easiest: run the script (Windows)

1. Open **Command Prompt** (cmd).
2. Go to the project folder, then into `scripts`:
   ```cmd
   cd C:\Users\21699\IdeaProjects\eventmodule\scripts
   set-mysql-packet-size.bat
   ```
3. When asked, enter your **MySQL root password** (same as in `application.properties`; leave empty if you have no password and press Enter).
4. Restart your Spring Boot app and try the event update again.

---

## Option 1: Run in MySQL manually (quick, until server restart)

Open a MySQL client (command line, MySQL Workbench, DBeaver, etc.) and run:

```sql
SET GLOBAL max_allowed_packet = 33554432;
```

(33,554,432 bytes = 32 MB)

Then **restart your Spring Boot application** so new connections use the new limit.

---

## Option 2: Permanent setting (recommended)

Edit the MySQL config file so the change survives restarts:

- **Windows:** `my.ini` (often `C:\ProgramData\MySQL\MySQL Server 8.0\my.ini` or in your MySQL install folder)
- **Linux/Mac:** `my.cnf` (e.g. `/etc/mysql/my.cnf` or `/etc/my.cnf`)

Under the `[mysqld]` section add or update:

```ini
[mysqld]
max_allowed_packet=32M
```

Then **restart the MySQL service** and restart your Spring Boot app.

---

## Optional: Ensure `image_url` column is large enough

If you haven’t already, run:

```sql
ALTER TABLE evenement MODIFY COLUMN image_url MEDIUMTEXT NULL;
```

After that, event updates with large base64 `imageUrl` should succeed.
