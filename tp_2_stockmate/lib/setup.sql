CREATE DATABASE SM2019;
CREATE USER 'sm' IDENTIFIED BY 'stockmate';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP ON SM2019.* TO sm@'%' IDENTIFIED BY 'stockmate';
\q
mysql -usm -pstockmate SM2019
DROP TABLE d;
CREATE TABLE d ( 
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