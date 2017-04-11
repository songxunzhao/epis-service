CREATE TABLE IF NOT EXISTS mandate_process (
  id SERIAL PRIMARY KEY,
  mandate_id INTEGER NOT NULL REFERENCES mandate,
  process_id VARCHAR NOT NULL,
  type VARCHAR NOT NULL,
  successful BOOLEAN,
  error_code integer,
  created_date TIMESTAMP NOT NULL
);
