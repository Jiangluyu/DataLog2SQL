INSERT INTO Customer (cName, cAge) VALUES ('John', 17);

INSERT INTO Customer (cName, cAge) VALUES ('Robert', 18);

INSERT INTO Customer (cName, cAge) VALUES ('Donne', 20);

INSERT INTO Seller (sName) VALUES ('Jake');

INSERT INTO Seller (sName) VALUES ('Robert');

INSERT INTO Seller (sName) VALUES ('Donne');

DROP VIEW IF EXISTS PROJECTION_RULE CASCADE;
CREATE VIEW PROJECTION_RULE AS
SELECT cName
FROM Customer;

DROP VIEW IF EXISTS PROJECTION_RULE2 CASCADE;
CREATE VIEW PROJECTION_RULE2 AS
SELECT cName, cAge
FROM Customer;

SELECTION_RULE(cName):-Customer(cName),cName='Jake'

DROP VIEW IF EXISTS INNER_JOIN_RULE CASCADE;
CREATE VIEW INNER_JOIN_RULE AS
SELECT Customer.cName, Seller.sName
FROM Customer INNER JOIN Seller
ON Customer.cName = Seller.sName;

DROP VIEW IF EXISTS INNER_JOIN_RULE2 CASCADE;
CREATE VIEW INNER_JOIN_RULE2 AS
SELECT Customer.cName, Customer.cAge, Seller.sName, Seller.sAge
FROM Customer INNER JOIN Seller
ON Customer.cName = Seller.sName and Customer.cAge = Seller.sAge;

DROP VIEW IF EXISTS LEFT_JOIN_RULE CASCADE;
CREATE VIEW LEFT_JOIN_RULE AS
SELECT Customer.cName, Seller.sName
FROM Customer LEFT JOIN Seller
ON Customer.cName = Seller.sName;

DROP VIEW IF EXISTS RIGHT_JOIN_RULE CASCADE;
CREATE VIEW RIGHT_JOIN_RULE AS
SELECT Customer.cName, Seller.sName
FROM Customer RIGHT JOIN Seller
ON Customer.cName = Seller.sName;

DROP VIEW IF EXISTS FULL_JOIN_RULE CASCADE;
CREATE VIEW FULL_JOIN_RULE AS
SELECT Customer.cName, Seller.sName
FROM Customer FULL JOIN Seller
ON Customer.cName = Seller.sName;

UNION_RULE(cName,sName):-Customer(cName);Seller(sName)

DIFFERENCE_RULE(cName):-Customer(cName),notSeller(cName)

