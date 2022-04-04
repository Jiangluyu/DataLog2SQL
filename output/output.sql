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

INNER_JOIN_RULE(cName):-ij(Customer(cName),Seller(sName),cName=sName)

LEFT_JOIN_RULE(cName,sName):-lj(Customer(cName),Seller(sName),cName=sName)

RIGHT_JOIN_RULE(cName,sName):-rj(Customer(cName),Seller(sName),cName=sName)

FULL_JOIN_RULE(cName,sName):-fj(Customer(cName),Seller(sName),cName=sName)

UNION_RULE(cName,sName):-Customer(cName);Seller(sName)

DIFFERENCE_RULE(cName):-Customer(cName),notSeller(cName)

