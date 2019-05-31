CREATE DATABASE SM2019;
CREATE USER 'sm' IDENTIFIED BY 'stockmate';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP ON SM2019.* TO sm@'%' IDENTIFIED BY 'stockmate';
\q
mysql -usm -pstockmate SM2019
DROP TABLE d;
CREATE TABLE d ( 
	ticker varchar(10) not null, 
	yr YEAR not null,
	prd CHAR(1) not null,
	tag varchar(20) not null,
	val FLOAT,
	loaddate DATETIME,
	CONSTRAINT pk_d PRIMARY KEY (ticker, yr, prd, tag) 
);
--REPLACE INTO d VALUES ('jnj',2010,0,'eps',2.4,now());