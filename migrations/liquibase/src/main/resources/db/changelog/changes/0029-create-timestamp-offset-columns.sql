--liquibase formatted sql

--changeset orkg:29 dbms:postgresql

ALTER TABLE contributors
    ADD COLUMN joined_at_offset_total_seconds integer NOT NULL DEFAULT 0;
ALTER TABLE observatory_filters
    ADD COLUMN created_at_offset_total_seconds integer NOT NULL DEFAULT 0;
ALTER TABLE images
    ADD COLUMN created_at_offset_total_seconds integer NOT NULL DEFAULT 0;
ALTER TABLE template_based_resource_snapshots
    ADD COLUMN created_at_offset_total_seconds integer NOT NULL DEFAULT 0;

UPDATE contributors
    SET joined_at_offset_total_seconds = 3600
    WHERE (joined_at > TIMESTAMP '2019-10-27T00:00Z' AND joined_at < TIMESTAMP '2020-03-30T01:00Z') OR
        (joined_at > TIMESTAMP '2020-10-29T00:00Z' AND joined_at < TIMESTAMP '2021-03-30T01:00Z') OR
        (joined_at > TIMESTAMP '2021-10-28T00:00Z' AND joined_at < TIMESTAMP '2022-03-30T01:00Z') OR
        (joined_at > TIMESTAMP '2022-10-27T00:00Z' AND joined_at < TIMESTAMP '2023-03-30T01:00Z') OR
        (joined_at > TIMESTAMP '2023-10-26T00:00Z' AND joined_at < TIMESTAMP '2024-03-30T01:00Z') OR
        (joined_at > TIMESTAMP '2024-10-31T00:00Z' AND joined_at < TIMESTAMP '2025-03-30T01:00Z');
UPDATE observatory_filters
    SET created_at_offset_total_seconds = 3600
    WHERE (created_at > TIMESTAMP '2019-10-27T00:00Z' AND created_at < TIMESTAMP '2020-03-30T01:00Z') OR
        (created_at > TIMESTAMP '2020-10-29T00:00Z' AND created_at < TIMESTAMP '2021-03-30T01:00Z') OR
        (created_at > TIMESTAMP '2021-10-28T00:00Z' AND created_at < TIMESTAMP '2022-03-30T01:00Z') OR
        (created_at > TIMESTAMP '2022-10-27T00:00Z' AND created_at < TIMESTAMP '2023-03-30T01:00Z') OR
        (created_at > TIMESTAMP '2023-10-26T00:00Z' AND created_at < TIMESTAMP '2024-03-30T01:00Z') OR
        (created_at > TIMESTAMP '2024-10-31T00:00Z' AND created_at < TIMESTAMP '2025-03-30T01:00Z');
UPDATE images
    SET created_at_offset_total_seconds = 3600
    WHERE (created_at > TIMESTAMP '2019-10-27T00:00Z' AND created_at < TIMESTAMP '2020-03-30T01:00Z') OR
        (created_at > TIMESTAMP '2020-10-29T00:00Z' AND created_at < TIMESTAMP '2021-03-30T01:00Z') OR
        (created_at > TIMESTAMP '2021-10-28T00:00Z' AND created_at < TIMESTAMP '2022-03-30T01:00Z') OR
        (created_at > TIMESTAMP '2022-10-27T00:00Z' AND created_at < TIMESTAMP '2023-03-30T01:00Z') OR
        (created_at > TIMESTAMP '2023-10-26T00:00Z' AND created_at < TIMESTAMP '2024-03-30T01:00Z') OR
        (created_at > TIMESTAMP '2024-10-31T00:00Z' AND created_at < TIMESTAMP '2025-03-30T01:00Z');
UPDATE template_based_resource_snapshots
    SET created_at_offset_total_seconds = 3600
    WHERE (created_at > TIMESTAMP '2019-10-27T00:00Z' AND created_at < TIMESTAMP '2020-03-30T01:00Z') OR
        (created_at > TIMESTAMP '2020-10-29T00:00Z' AND created_at < TIMESTAMP '2021-03-30T01:00Z') OR
        (created_at > TIMESTAMP '2021-10-28T00:00Z' AND created_at < TIMESTAMP '2022-03-30T01:00Z') OR
        (created_at > TIMESTAMP '2022-10-27T00:00Z' AND created_at < TIMESTAMP '2023-03-30T01:00Z') OR
        (created_at > TIMESTAMP '2023-10-26T00:00Z' AND created_at < TIMESTAMP '2024-03-30T01:00Z') OR
        (created_at > TIMESTAMP '2024-10-31T00:00Z' AND created_at < TIMESTAMP '2025-03-30T01:00Z');

UPDATE contributors
    SET joined_at_offset_total_seconds = 7200
    WHERE (joined_at > TIMESTAMP '2019-03-31T01:00Z' AND joined_at < TIMESTAMP '2019-10-27T00:00Z') OR
        (joined_at > TIMESTAMP '2020-03-30T01:00Z' AND joined_at < TIMESTAMP '2020-10-29T00:00Z') OR
        (joined_at > TIMESTAMP '2021-03-30T01:00Z' AND joined_at < TIMESTAMP '2021-10-28T00:00Z') OR
        (joined_at > TIMESTAMP '2022-03-30T01:00Z' AND joined_at < TIMESTAMP '2022-10-27T00:00Z') OR
        (joined_at > TIMESTAMP '2023-03-30T01:00Z' AND joined_at < TIMESTAMP '2023-10-26T00:00Z') OR
        (joined_at > TIMESTAMP '2024-03-30T01:00Z' AND joined_at < TIMESTAMP '2024-10-31T00:00Z') OR
        (joined_at > TIMESTAMP '2025-03-30T01:00Z' AND joined_at < TIMESTAMP '2025-10-26T01:00Z');
UPDATE observatory_filters
    SET created_at_offset_total_seconds = 7200
    WHERE (created_at > TIMESTAMP '2019-03-31T01:00Z' AND created_at < TIMESTAMP '2019-10-27T00:00Z') OR
        (created_at > TIMESTAMP '2020-03-30T01:00Z' AND created_at < TIMESTAMP '2020-10-29T00:00Z') OR
        (created_at > TIMESTAMP '2021-03-30T01:00Z' AND created_at < TIMESTAMP '2021-10-28T00:00Z') OR
        (created_at > TIMESTAMP '2022-03-30T01:00Z' AND created_at < TIMESTAMP '2022-10-27T00:00Z') OR
        (created_at > TIMESTAMP '2023-03-30T01:00Z' AND created_at < TIMESTAMP '2023-10-26T00:00Z') OR
        (created_at > TIMESTAMP '2024-03-30T01:00Z' AND created_at < TIMESTAMP '2024-10-31T00:00Z') OR
        (created_at > TIMESTAMP '2025-03-30T01:00Z' AND created_at < TIMESTAMP '2025-10-26T01:00Z');
UPDATE images
    SET created_at_offset_total_seconds = 7200
    WHERE (created_at > TIMESTAMP '2019-03-31T01:00Z' AND created_at < TIMESTAMP '2019-10-27T00:00Z') OR
        (created_at > TIMESTAMP '2020-03-30T01:00Z' AND created_at < TIMESTAMP '2020-10-29T00:00Z') OR
        (created_at > TIMESTAMP '2021-03-30T01:00Z' AND created_at < TIMESTAMP '2021-10-28T00:00Z') OR
        (created_at > TIMESTAMP '2022-03-30T01:00Z' AND created_at < TIMESTAMP '2022-10-27T00:00Z') OR
        (created_at > TIMESTAMP '2023-03-30T01:00Z' AND created_at < TIMESTAMP '2023-10-26T00:00Z') OR
        (created_at > TIMESTAMP '2024-03-30T01:00Z' AND created_at < TIMESTAMP '2024-10-31T00:00Z') OR
        (created_at > TIMESTAMP '2025-03-30T01:00Z' AND created_at < TIMESTAMP '2025-10-26T01:00Z');
UPDATE template_based_resource_snapshots
    SET created_at_offset_total_seconds = 7200
    WHERE (created_at > TIMESTAMP '2019-03-31T01:00Z' AND created_at < TIMESTAMP '2019-10-27T00:00Z') OR
        (created_at > TIMESTAMP '2020-03-30T01:00Z' AND created_at < TIMESTAMP '2020-10-29T00:00Z') OR
        (created_at > TIMESTAMP '2021-03-30T01:00Z' AND created_at < TIMESTAMP '2021-10-28T00:00Z') OR
        (created_at > TIMESTAMP '2022-03-30T01:00Z' AND created_at < TIMESTAMP '2022-10-27T00:00Z') OR
        (created_at > TIMESTAMP '2023-03-30T01:00Z' AND created_at < TIMESTAMP '2023-10-26T00:00Z') OR
        (created_at > TIMESTAMP '2024-03-30T01:00Z' AND created_at < TIMESTAMP '2024-10-31T00:00Z') OR
        (created_at > TIMESTAMP '2025-03-30T01:00Z' AND created_at < TIMESTAMP '2025-10-26T01:00Z');
