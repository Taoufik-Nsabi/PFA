IF OBJECT_ID('dbo.stades', 'U') IS NOT NULL AND COL_LENGTH('dbo.stades', 'api_football_id') IS NULL
BEGIN
    ALTER TABLE dbo.stades ADD api_football_id INT NULL;
END
GO

IF OBJECT_ID('dbo.equipes', 'U') IS NOT NULL AND COL_LENGTH('dbo.equipes', 'api_football_id') IS NULL
BEGIN
    ALTER TABLE dbo.equipes ADD api_football_id INT NULL;
END
GO

IF OBJECT_ID('dbo.matchs', 'U') IS NOT NULL AND COL_LENGTH('dbo.matchs', 'api_football_id') IS NULL
BEGIN
    ALTER TABLE dbo.matchs ADD api_football_id INT NULL;
END
GO

IF OBJECT_ID('dbo.status_matchs', 'U') IS NOT NULL AND COL_LENGTH('dbo.status_matchs', 'score') IS NULL
BEGIN
    ALTER TABLE dbo.status_matchs ADD score VARCHAR(20) NULL;
END
GO

IF OBJECT_ID('dbo.status_matchs', 'U') IS NOT NULL AND COL_LENGTH('dbo.status_matchs', 'duree_minutes') IS NULL
BEGIN
    ALTER TABLE dbo.status_matchs ADD duree_minutes INT NULL;
END
GO

IF OBJECT_ID('dbo.status_matchs', 'U') IS NOT NULL
   AND COL_LENGTH('dbo.status_matchs', 'duree') IS NOT NULL
   AND COL_LENGTH('dbo.status_matchs', 'duree_minutes') IS NOT NULL
BEGIN
    EXEC('UPDATE dbo.status_matchs SET duree_minutes = COALESCE(duree_minutes, duree)');
END
GO

IF OBJECT_ID('dbo.status_matchs', 'U') IS NOT NULL AND COL_LENGTH('dbo.status_matchs', 'etat') IS NULL
BEGIN
    ALTER TABLE dbo.status_matchs ADD etat VARCHAR(20) NULL;
END
GO

IF OBJECT_ID('dbo.status_matchs', 'U') IS NOT NULL AND COL_LENGTH('dbo.status_matchs', 'annule') IS NOT NULL
BEGIN
    EXEC('UPDATE dbo.status_matchs SET etat = ''ANNULE'' WHERE etat IS NULL AND ISNULL(annule, 0) = 1');
END
GO

IF OBJECT_ID('dbo.status_matchs', 'U') IS NOT NULL AND COL_LENGTH('dbo.status_matchs', 'reporte') IS NOT NULL
BEGIN
    EXEC('UPDATE dbo.status_matchs SET etat = ''REPORTE'' WHERE etat IS NULL AND ISNULL(reporte, 0) = 1');
END
GO

IF OBJECT_ID('dbo.status_matchs', 'U') IS NOT NULL AND COL_LENGTH('dbo.status_matchs', 'suspendu') IS NOT NULL
BEGIN
    EXEC('UPDATE dbo.status_matchs SET etat = ''SUSPENDU'' WHERE etat IS NULL AND ISNULL(suspendu, 0) = 1');
END
GO

IF OBJECT_ID('dbo.status_matchs', 'U') IS NOT NULL AND COL_LENGTH('dbo.status_matchs', 'en_cours') IS NOT NULL
BEGIN
    EXEC('UPDATE dbo.status_matchs SET etat = ''EN_COURS'' WHERE etat IS NULL AND ISNULL(en_cours, 0) = 1');
END
GO

IF OBJECT_ID('dbo.status_matchs', 'U') IS NOT NULL AND COL_LENGTH('dbo.status_matchs', 'termine') IS NOT NULL
BEGIN
    EXEC('UPDATE dbo.status_matchs SET etat = ''TERMINE'' WHERE etat IS NULL AND ISNULL(termine, 0) = 1');
END
GO

IF OBJECT_ID('dbo.status_matchs', 'U') IS NOT NULL
BEGIN
    UPDATE dbo.status_matchs
    SET etat = CASE
        WHEN UPPER(LTRIM(RTRIM(ISNULL(etat, '')))) = 'SUSP' THEN 'SUSPENDU'
        WHEN UPPER(LTRIM(RTRIM(ISNULL(etat, '')))) IN ('PROGRAMME', 'EN_COURS', 'TERMINE', 'REPORTE', 'ANNULE', 'SUSPENDU')
            THEN UPPER(LTRIM(RTRIM(etat)))
        ELSE 'PROGRAMME'
    END;
END
GO

IF OBJECT_ID('dbo.status_matchs', 'U') IS NOT NULL
BEGIN
    ALTER TABLE dbo.status_matchs ALTER COLUMN score VARCHAR(20) NULL;
    ALTER TABLE dbo.status_matchs ALTER COLUMN duree_minutes INT NULL;
    ALTER TABLE dbo.status_matchs ALTER COLUMN etat VARCHAR(20) NOT NULL;
END
GO

IF OBJECT_ID('dbo.status_matchs', 'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.default_constraints dc
       INNER JOIN sys.columns c
           ON c.object_id = dc.parent_object_id
          AND c.column_id = dc.parent_column_id
       WHERE dc.parent_object_id = OBJECT_ID('dbo.status_matchs')
         AND c.name = 'etat'
   )
BEGIN
    ALTER TABLE dbo.status_matchs
    ADD CONSTRAINT DF_status_matchs_etat_legacy DEFAULT ('PROGRAMME') FOR etat;
END
GO

IF OBJECT_ID('dbo.matchs', 'U') IS NOT NULL
   AND EXISTS (
       SELECT 1
       FROM sys.columns c
       INNER JOIN sys.types t
           ON t.user_type_id = c.user_type_id
       WHERE c.object_id = OBJECT_ID('dbo.matchs')
         AND c.name = 'date_heure'
         AND t.name IN ('varchar', 'nvarchar', 'char', 'nchar', 'text', 'ntext')
   )
   AND COL_LENGTH('dbo.matchs', 'date_heure_migrated') IS NULL
BEGIN
    ALTER TABLE dbo.matchs ADD date_heure_migrated DATETIME2(6) NULL;
END
GO

IF OBJECT_ID('dbo.matchs', 'U') IS NOT NULL
   AND COL_LENGTH('dbo.matchs', 'date_heure') IS NOT NULL
   AND COL_LENGTH('dbo.matchs', 'date_heure_migrated') IS NOT NULL
BEGIN
    EXEC sp_executesql N'
        UPDATE dbo.matchs
        SET date_heure_migrated = COALESCE(
            TRY_CONVERT(DATETIME2(6), date_heure, 127),
            TRY_CONVERT(DATETIME2(6), date_heure, 126),
            TRY_CONVERT(DATETIME2(6), date_heure, 121),
            TRY_CONVERT(DATETIME2(6), date_heure, 120),
            TRY_CONVERT(DATETIME2(6), date_heure)
        )
        WHERE date_heure IS NOT NULL
          AND date_heure_migrated IS NULL;
    ';
END
GO

IF OBJECT_ID('dbo.matchs', 'U') IS NOT NULL
   AND COL_LENGTH('dbo.matchs', 'date_heure') IS NOT NULL
   AND COL_LENGTH('dbo.matchs', 'date_heure_migrated') IS NOT NULL
BEGIN
    EXEC sp_executesql N'
        IF EXISTS (
            SELECT 1
            FROM dbo.matchs
            WHERE date_heure IS NOT NULL
              AND date_heure_migrated IS NULL
        )
        BEGIN
            THROW 50001, ''Impossible de migrer les valeurs legacy de matchs.date_heure vers DATETIME2. Corrigez les donnees puis relancez Flyway.'', 1;
        END;
    ';
END
GO

IF OBJECT_ID('dbo.matchs', 'U') IS NOT NULL
   AND COL_LENGTH('dbo.matchs', 'date_heure') IS NOT NULL
   AND COL_LENGTH('dbo.matchs', 'date_heure_migrated') IS NOT NULL
BEGIN
    ALTER TABLE dbo.matchs DROP COLUMN date_heure;
    EXEC sp_rename 'dbo.matchs.date_heure_migrated', 'date_heure', 'COLUMN';
END
GO

IF OBJECT_ID('dbo.matchs', 'U') IS NOT NULL
BEGIN
    ALTER TABLE dbo.matchs ALTER COLUMN date_heure DATETIME2(6) NOT NULL;
END
GO

IF OBJECT_ID('dbo.stades', 'U') IS NOT NULL
   AND COL_LENGTH('dbo.stades', 'api_football_id') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE name = 'idx_stades_api_football_id'
         AND object_id = OBJECT_ID('dbo.stades')
   )
BEGIN
    CREATE INDEX idx_stades_api_football_id ON dbo.stades (api_football_id);
END
GO

IF OBJECT_ID('dbo.equipes', 'U') IS NOT NULL
   AND COL_LENGTH('dbo.equipes', 'api_football_id') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE name = 'idx_equipes_api_football_id'
         AND object_id = OBJECT_ID('dbo.equipes')
   )
BEGIN
    CREATE INDEX idx_equipes_api_football_id ON dbo.equipes (api_football_id);
END
GO

IF OBJECT_ID('dbo.matchs', 'U') IS NOT NULL
   AND COL_LENGTH('dbo.matchs', 'api_football_id') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE name = 'idx_matchs_api_football_id'
         AND object_id = OBJECT_ID('dbo.matchs')
   )
BEGIN
    CREATE INDEX idx_matchs_api_football_id ON dbo.matchs (api_football_id);
END
GO

IF OBJECT_ID('dbo.matchs', 'U') IS NOT NULL
   AND COL_LENGTH('dbo.matchs', 'status_match_id') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE name = 'UQ_matchs_status_match_id'
         AND object_id = OBJECT_ID('dbo.matchs')
   )
BEGIN
    CREATE UNIQUE INDEX UQ_matchs_status_match_id ON dbo.matchs (status_match_id)
    WHERE status_match_id IS NOT NULL;
END
GO
