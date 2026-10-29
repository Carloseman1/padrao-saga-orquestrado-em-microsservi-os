ALTER TABLE livro ADD COLUMN preco NUMERIC(10, 2);

UPDATE livro SET preco = 39.90 WHERE codigo = 'DOM_CASMURRO';
UPDATE livro SET preco = 29.90 WHERE codigo = 'O_CORTICO';
UPDATE livro SET preco = 25.00 WHERE codigo = 'IRACEMA';
UPDATE livro SET preco = 39.90 WHERE codigo = 'MEMORIAS_POSTUMAS';
UPDATE livro SET preco = 0.00 WHERE preco IS NULL;

ALTER TABLE livro ALTER COLUMN preco SET NOT NULL;
