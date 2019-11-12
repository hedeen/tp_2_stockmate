CREATE DATABASE SM2019;
CREATE USER 'sm' IDENTIFIED BY 'stockmate';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP ON SM2019.* TO sm@'%' IDENTIFIED BY 'stockmate';
\q
mysql -usm -pstockmate SM2019
DROP TABLE SM2019.d;
CREATE TABLE SM2019.d ( 
	tkr char(4) not null,
	yr YEAR not null,
	prd CHAR(1) not null,
	esb FLOAT,
	esd FLOAT,
	ern FLOAT,
	shb FLOAT,
	shd FLOAT,
	pft FLOAT,
	gpf FLOAT,
	ldt DATETIME,
	CONSTRAINT pk_d PRIMARY KEY (tkr, yr, prd) 
);
--REPLACE INTO d VALUES ('jnj',2010,0,'eps',2.4,now());
CREATE TABLE SM2019.calcs ( 
	tkr char(4) PRIMARY KEY,
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
	ldt DATETIME DEFAULT SYSDATE()
);