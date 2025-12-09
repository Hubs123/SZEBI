
-- Tabela pomiarów energii
create table if not exists measurements (
    id                  bigserial primary key,
    device_id           integer not null references devices(id) on delete cascade,
    timestamp           timestamp with time zone not null,
    power_output_w      double precision not null,
    grid_feed_in_kwh    double precision not null,
    grid_consumption_kwh double precision not null
);

alter table measurements owner to postgres;

grant select, update, usage on sequence measurements_id_seq to anon;
grant select, update, usage on sequence measurements_id_seq to authenticated;
grant select, update, usage on sequence measurements_id_seq to service_role;

grant delete, insert, references, select, trigger, truncate, update on measurements to anon;
grant delete, insert, references, select, trigger, truncate, update on measurements to authenticated;
grant delete, insert, references, select, trigger, truncate, update on measurements to service_role;

create index if not exists idx_measurements_device_time
    on measurements (device_id, timestamp);


-- Tabela statystyk energii
create table if not exists energy_stats (
    id              bigserial primary key,
    device_id       integer not null references devices(id) on delete cascade,
    start_time      timestamp with time zone not null,
    end_time        timestamp with time zone not null,
    avg_power_w     double precision not null,
    daily_kwh       double precision not null,
    annual_kwh      double precision not null,
    min_power_w     double precision not null,
    max_power_w     double precision not null
);

alter table energy_stats owner to postgres;

grant select, update, usage on sequence energy_stats_id_seq to anon;
grant select, update, usage on sequence energy_stats_id_seq to authenticated;
grant select, update, usage on sequence energy_stats_id_seq to service_role;

grant delete, insert, references, select, trigger, truncate, update on energy_stats to anon;
grant delete, insert, references, select, trigger, truncate, update on energy_stats to authenticated;
grant delete, insert, references, select, trigger, truncate, update on energy_stats to service_role;

create index if not exists idx_energy_stats_device_range
    on energy_stats (device_id, start_time, end_time);


-- Tabela predykcji
create table if not exists predictions (
    id                  bigserial primary key,
    device_id           integer not null references devices(id) on delete cascade,
    timestamp           timestamp with time zone not null,
    predicted_for_date  date not null,
    value_kwh           double precision not null,
    model_id            integer not null
);

alter table predictions owner to postgres;

grant select, update, usage on sequence predictions_id_seq to anon;
grant select, update, usage on sequence predictions_id_seq to authenticated;
grant select, update, usage on sequence predictions_id_seq to service_role;

grant delete, insert, references, select, trigger, truncate, update on predictions to anon;
grant delete, insert, references, select, trigger, truncate, update on predictions to authenticated;
grant delete, insert, references, select, trigger, truncate, update on predictions to service_role;

create index if not exists idx_predictions_device_date
    on predictions (device_id, predicted_for_date);

create index if not exists idx_predictions_device_timestamp
    on predictions (device_id, timestamp);


-- Tabela raportów
create table if not exists reports (
    id              bigserial primary key,
    type            varchar(255) not null,
    device_id       integer not null references devices(id) on delete cascade,
    start_time      timestamp with time zone not null,
    end_time        timestamp with time zone not null,
    stats_id        bigint references energy_stats(id) on delete set null,
    prediction_id   bigint references predictions(id) on delete set null,
    text_summary    text not null,
    plots_paths     jsonb not null default '{}'::jsonb,
    created_at      timestamp with time zone not null default now()
);

alter table reports owner to postgres;

grant select, update, usage on sequence reports_id_seq to anon;
grant select, update, usage on sequence reports_id_seq to authenticated;
grant select, update, usage on sequence reports_id_seq to service_role;

grant delete, insert, references, select, trigger, truncate, update on reports to anon;
grant delete, insert, references, select, trigger, truncate, update on reports to authenticated;
grant delete, insert, references, select, trigger, truncate, update on reports to service_role;

create index if not exists idx_reports_device_created
    on reports (device_id, created_at);

create index if not exists idx_reports_type_created
    on reports (type, created_at);

