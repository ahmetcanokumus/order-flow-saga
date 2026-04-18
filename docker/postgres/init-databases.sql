-- Per-service databases (choreography saga: logical DB isolation on one Postgres instance for local dev)
CREATE DATABASE orderdb;
CREATE DATABASE stockdb;
CREATE DATABASE paymentdb;

GRANT ALL PRIVILEGES ON DATABASE orderdb TO postgres;
GRANT ALL PRIVILEGES ON DATABASE stockdb TO postgres;
GRANT ALL PRIVILEGES ON DATABASE paymentdb TO postgres;
