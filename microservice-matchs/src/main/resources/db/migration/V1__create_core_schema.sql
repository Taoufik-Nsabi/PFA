IF OBJECT_ID('dbo.stades', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.stades (
        id INT IDENTITY(1,1) NOT NULL,
        nom VARCHAR(150) NOT NULL,
        ville VARCHAR(100) NOT NULL,
        api_football_id INT NULL,
        capacite INT NOT NULL,
        CONSTRAINT PK_stades PRIMARY KEY (id),
        CONSTRAINT CK_stades_capacite_positive CHECK (capacite > 0)
    );
END
GO

IF OBJECT_ID('dbo.equipes', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.equipes (
        id INT IDENTITY(1,1) NOT NULL,
        name VARCHAR(150) NOT NULL,
        pays VARCHAR(100) NOT NULL,
        api_football_id INT NULL,
        entraineur VARCHAR(150) NULL,
        domicile BIT NOT NULL CONSTRAINT DF_equipes_domicile DEFAULT (0),
        exterieur BIT NOT NULL CONSTRAINT DF_equipes_exterieur DEFAULT (0),
        CONSTRAINT PK_equipes PRIMARY KEY (id)
    );
END
GO

IF OBJECT_ID('dbo.status_matchs', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.status_matchs (
        id INT IDENTITY(1,1) NOT NULL,
        duree_minutes INT NULL,
        score VARCHAR(20) NULL,
        etat VARCHAR(20) NOT NULL CONSTRAINT DF_status_matchs_etat DEFAULT ('PROGRAMME'),
        CONSTRAINT PK_status_matchs PRIMARY KEY (id),
        CONSTRAINT CK_status_matchs_duree_positive CHECK (duree_minutes IS NULL OR duree_minutes >= 0)
    );
END
GO

IF OBJECT_ID('dbo.matchs', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.matchs (
        id INT IDENTITY(1,1) NOT NULL,
        date_heure DATETIME2(6) NOT NULL,
        api_football_id INT NULL,
        stade_id INT NOT NULL,
        equipe_locale_id INT NOT NULL,
        equipe_visiteur_id INT NOT NULL,
        status_match_id INT NULL,
        CONSTRAINT PK_matchs PRIMARY KEY (id),
        CONSTRAINT FK_matchs_stade FOREIGN KEY (stade_id) REFERENCES dbo.stades (id),
        CONSTRAINT FK_matchs_equipe_locale FOREIGN KEY (equipe_locale_id) REFERENCES dbo.equipes (id),
        CONSTRAINT FK_matchs_equipe_visiteur FOREIGN KEY (equipe_visiteur_id) REFERENCES dbo.equipes (id),
        CONSTRAINT FK_matchs_status_match FOREIGN KEY (status_match_id) REFERENCES dbo.status_matchs (id)
    );
END
GO

IF COL_LENGTH('dbo.stades', 'api_football_id') IS NOT NULL
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

IF COL_LENGTH('dbo.equipes', 'api_football_id') IS NOT NULL
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

IF COL_LENGTH('dbo.matchs', 'api_football_id') IS NOT NULL
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

IF COL_LENGTH('dbo.matchs', 'status_match_id') IS NOT NULL
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
