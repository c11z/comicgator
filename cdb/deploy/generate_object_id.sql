-- Deploy generate_object_id

BEGIN;

CREATE OR REPLACE FUNCTION cg.generate_object_id() RETURNS varchar AS $$
    DECLARE
        time_component bigint;
        machine_id int := FLOOR(random() * 16777215);
        process_id int;
        seq_id bigint := FLOOR(random() * 16777215);
        result varchar:= '';
    BEGIN
        SELECT FLOOR(EXTRACT(EPOCH FROM clock_timestamp())) INTO time_component;
        SELECT pg_backend_pid() INTO process_id;

        result := result || lpad(to_hex(time_component), 8, '0');
        result := result || lpad(to_hex(machine_id), 6, '0');
        result := result || lpad(to_hex(process_id), 4, '0');
        result := result || lpad(to_hex(seq_id), 6, '0');
        RETURN result;
    END;
$$ LANGUAGE PLPGSQL;

COMMIT;