CREATE DATABASE SM2019;
CREATE USER 'sm' IDENTIFIED BY 'stockmate';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP ON SM2019.* TO sm@'%' IDENTIFIED BY 'stockmate';
\q
mysql -usm -pstockmate SM2019
DROP TABLE SM2019.data;
CREATE TABLE SM2019.data ( 
	tkr varchar(5) not null,
	yr YEAR not null,
	prd CHAR(1) not null,
	hst char(1) DEFAULT 'N',
	esb FLOAT,
	esd FLOAT,
	ern FLOAT,
	shb FLOAT,
	shd FLOAT,
	pft FLOAT,
	gpf FLOAT,
	sdt DATETIME,
	edt DATETIME,
	ldt DATETIME,
	CONSTRAINT pk_d PRIMARY KEY (tkr, yr, prd) 
);
--
DROP TABLE SM2019.filings;
CREATE TABLE SM2019.filings ( 
	tkr varchar(5) not null,
	ftp char(6) not null,
	fdt DATE,
	ldt DATETIME DEFAULT NOW(),
	CONSTRAINT pk_filings PRIMARY KEY (tkr, ftp, fdt) 
);
--
CREATE OR REPLACE VIEW SM2019.EPS AS
SELECT
	TKR,
	YR,
	PRD,
	COALESCE(
		ESD,
		ERN/SHD,
		ESB,
		ERN/SHB)
	AS EPS,
	CASE 
		WHEN ESD IS NOT NULL THEN 'ESD'
		WHEN ERN/SHD IS NOT NULL THEN 'ERN/SHD'
		WHEN ESB IS NOT NULL THEN 'ESB'
		WHEN ERN/SHB IS NOT NULL THEN 'ERN/SHB'
	END AS SRC
FROM SM2019.data;
--REPLACE INTO d VALUES ('jnj',2010,0,'eps',2.4,now());
DROP TABLE SM2019.calcs;
CREATE TABLE SM2019.calcs ( 
	tkr varchar(5) PRIMARY KEY,
	r3 FLOAT,
	m3 FLOAT,
	b3 FLOAT,
	r5 FLOAT,
	m5 FLOAT,
	b5 FLOAT,	
	r9 FLOAT,
	m9 FLOAT,
	b9 FLOAT,
	r10 FLOAT,
	m10 FLOAT,
	b10 FLOAT,
	cdt DATETIME DEFAULT SYSDATE()
);
--
--
CREATE OR REPLACE VIEW SM2019.mp AS
#the below determines the missing periods in the eps data table
WITH 
	f AS (
		SELECT DISTINCT tkr, yr, seq AS prd FROM seq_0_to_4, eps
	), t AS (
		SELECT tkr, yr, prd FROM eps
)
SELECT tkr,yr,prd missing_prd FROM f WHERE (tkr,yr,prd) NOT IN (SELECT * FROM t);
--
--
CREATE OR REPLACE VIEW SM2019.epsgf AS
#the view below fills the gap on any missing quarterly data by subtracing the Qs from the annual
WITH T AS (
	SELECT tkr, yr, prd, eps, src FROM eps 
	UNION ALL
	SELECT e.tkr, e.yr, mp.missing_prd,
		(SELECT eps FROM eps WHERE eps.TKR = e.TKR AND eps.YR = e.YR AND eps.PRD = 0) -
		(SELECT eps FROM eps WHERE eps.TKR = e.TKR AND eps.YR = e.YR AND eps.PRD = 1) -
		(SELECT eps FROM eps WHERE eps.TKR = e.TKR AND eps.YR = e.YR AND eps.PRD = 2) -
		(SELECT eps FROM eps WHERE eps.TKR = e.TKR AND eps.YR = e.YR AND eps.PRD = 3) 
		, 'SUM' AS src
	FROM eps e
	LEFT JOIN mp ON e.TKR = mp.tkr AND e.YR = mp.yr
	WHERE e.prd = 0
	AND (e.tkr,e.yr) IN #must be in group that is missing a quarter
		(SELECT tkr, yr 
		FROM eps 
		GROUP BY tkr, yr 
		HAVING COUNT(*) = 4) 
	AND (e.tkr,e.yr) NOT IN (SELECT tkr, yr FROM eps WHERE prd = 4)
)
DROP TABLE rprices;
CREATE TABLE rprices (
#recent close prices
	tkr varchar(5) NOT NULL,
	cdt DATE NOT NULL,
	cpr DECIMAL(15,4) NULL,
	ldt DATETIME DEFAULT NOW(),
	PRIMARY KEY (tkr, cdt)
);
ALTER TABLE SM2019.data ADD COLUMN hst char(1) DEFAULT 'N' AFTER prd;
INSERT IGNORE INTO SM2019.data (tkr,yr,prd,hst,esb,esd,ern,ldt) SELECT ticker,yr,0,'Y',eps,epsdil,netinc,loaddate FROM S.D;
INSERT IGNORE INTO SM2019.data (tkr,yr,prd,hst,esb,esd,ern,ldt) SELECT ticker,yr,RANK() OVER (PARTITION BY ticker, yr ORDER BY mon),'Y',eps,epsdil,netinc,loaddate FROM S.Q;
