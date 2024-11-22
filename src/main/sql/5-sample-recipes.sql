INSERT INTO recipe (author, name, description, category, pescetarian, vegetarian, vegan, gluten_free) VALUES
  (1, 'spicy chip-pea stew', 'a hearty stew made with chickpeas and spices', 'soup', 1, 1, 1, 1);

INSERT INTO recipe_ingredient (recipe, ingredient, quantity, unit) VALUES
  (1, 1, 2, NULL),          -- tomato
  (1, 16, 2, 'cloves'),     -- garlic
  (1, 15, 1, NULL),         -- onion
  (1, 20, 2, 'cups'),       -- chickpeas
  (1, 17, 1, 'tablespoon'), -- olive oil
  (1, 64, 1, 'teaspoon'),   -- paprika
  (1, 65, 1, 'teaspoon'),   -- cumin
  (1, 66, 1, 'pinch'),      -- salt
  (1, 67, 1, 'pinch');      -- pepper

INSERT INTO recipe_skill (recipe, skill) VALUES
  (1, 15), -- chop
  (1, 16), -- mince
  (1, 14), -- dice
  (1, 24), -- sauté
  (1, 25); -- boil

INSERT INTO recipe_equipment (recipe, equipment) VALUES
  (1, 1),  -- knife
  (1, 15), -- stove
  (1, 26); -- pot

INSERT INTO recipe_step (recipe, step, `order`) VALUES
  (1, 'chop the onion', 1),
  (1, 'mince the garlic', 2),
  (1, 'dice the tomatoes', 3),
  (1, 'heat olive oil in a pot over medium heat', 4),
  (1, 'add chopped onions and sauté until translucent', 5),
  (1, 'add minced garlic and cook for 1 minute', 6),
  (1, 'stir in cumin and paprika', 7),
  (1, 'add chickpeas and diced tomatoes, stir well', 8),
  (1, 'add quinoa and 2 cups of water, bring to a boil', 9),
  (1, 'reduce heat and simmer for 20 minutes until quinoa is cooked', 10),
  (1, 'season with salt and pepper to taste', 11);

INSERT INTO recipe (author, name, description, category, pescetarian, vegetarian, vegan, gluten_free) VALUES
  (4, 'creamy spinach pasta', 'a delightful pasta dish with a creamy spinach sauce', 'main', 1, 1, 0, 0);

INSERT INTO recipe_ingredient (recipe, ingredient, quantity, unit) VALUES
  (2, 40, 200, 'grams'),     -- pasta
  (2, 10, 3, 'cups'),        -- spinach
  (2, 17, 2, 'tablespoons'), -- olive oil
  (2, 16, 2, 'cloves'),      -- garlic
  (2, 68, 1, 'cup'),         -- cream
  (2, 66, 1, 'teaspoon'),    -- salt
  (2, 67, 1, 'teaspoon'),    -- pepper
  (2, 36, 1, 'teaspoon');    -- cheese

INSERT INTO recipe_skill (recipe, skill) VALUES
  (2, 14), -- boil
  (2, 24), -- sauté
  (2, 15); -- chop

INSERT INTO recipe_equipment (recipe, equipment) VALUES
  (2, 1),  -- knife
  (2, 15), -- stove
  (2, 26), -- pot
  (2, 14); -- pan

INSERT INTO recipe_step (recipe, step, `order`) VALUES
  (2, 'boil water in a pot', 1),
  (2, 'add pasta and cook according to package instructions', 2),
  (2, 'in a separate pan, heat olive oil over medium heat', 3),
  (2, 'chop garlic and add to the pan, sauté until fragrant', 4),
  (2, 'add spinach and cook until wilted', 5),
  (2, 'stir in cream and season with salt and pepper', 6),
  (2, 'drain the pasta and combine with the spinach sauce', 7),
  (2, 'serve hot, garnished with grated cheese if desired', 8);

INSERT INTO recipe (author, name, description, category, pescetarian, vegetarian, vegan, gluten_free) VALUES
  (11, 'lemon garlic chicken', 'tender chicken marinated in lemon and garlic, then grilled to perfection', 'main', 0, 0, 0, 1);

INSERT INTO recipe_ingredient (recipe, ingredient, quantity, unit) VALUES
  (3, 23, 4, NULL),       -- chicken breast
  (3, 16, 4, 'cloves'),      -- garlic
  (3, 17, 1/4, 'cup'),       -- olive oil
  (3, 66, 1, 'teaspoon'),    -- salt
  (3, 67, 1, 'teaspoon'),    -- pepper
  (3, 70, 1, 'tablespoon'),  -- thyme
  (3, 69, 1, NULL);          -- lemon

INSERT INTO recipe_skill (recipe, skill) VALUES
  (3, 30); -- grill

INSERT INTO recipe_equipment (recipe, equipment) VALUES
  (3, 1),  -- knife
  (3, 25), -- grill
  (3, 7); -- bowl

INSERT INTO recipe_step (recipe, step, `order`) VALUES
  (3, 'in a bowl, mix olive oil, minced garlic, lemon juice, salt, pepper, and thyme', 1),
  (3, 'add chicken breasts to the marinade and coat well', 2),
  (3, 'cover and refrigerate for at least 30 minutes', 3),
  (3, 'preheat the grill to medium-high heat', 4),
  (3, 'remove chicken from marinade and grill for 6-7 minutes on each side until cooked through', 5),
  (3, 'let the chicken rest for a few minutes before slicing', 6),
  (3, 'serve with a side of your choice', 7);

INSERT INTO recipe (author, name, description, category, pescetarian, vegetarian, vegan, gluten_free) VALUES
  (16, 'chocolate avocado mousse', 'a rich and creamy dessert made with avocados and cocoa', 'dessert', 1, 1, 1, 1);

INSERT INTO recipe_ingredient (recipe, ingredient, quantity, unit) VALUES
  (4, 71, 2, NULL),         -- avocados
  (4, 51, 1, 'cup'),        -- cocoa powder
  (4, 72, 1/2, 'cup'),      -- maple syrup
  (4, 50, 1, 'teaspoon'),   -- vanilla extract
  (4, 66, 1, 'pinch');      -- salt

INSERT INTO recipe_skill (recipe, skill) VALUES
  (4, 33); -- blend

INSERT INTO recipe_equipment (recipe, equipment) VALUES
  (4, 28), -- blender
  (4, 1),  -- knife
  (4, 7); -- bowl

INSERT INTO recipe_step (recipe, step, `order`) VALUES
  (4, 'cut avocados in half and remove the pit', 1),
  (4, 'scoop the avocado flesh into a blender', 2),
  (4, 'add cocoa powder, maple syrup, vanilla, and salt', 3),
  (4, 'blend until smooth and creamy', 4),
  (4, 'taste and adjust sweetness if necessary', 5),
  (4, 'transfer mousse to serving bowls and chill for 30 minutes', 6),
  (4, 'serve with berries or nuts on top if desired', 7);