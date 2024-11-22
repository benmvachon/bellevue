SET @p_output_id = 0; -- Initialize the variable

CALL add_review(2, 1, 'splendidly', 'my cat, Monkey, loved this', @p_output_id);
CALL add_review(20, 1, 'muddlingly', 'I dont think I like chickpeas', @p_output_id);
CALL add_review(3, 1, 'disastrously', 'I burned down the house', @p_output_id);
CALL add_review(7, 1, 'jubilantly', 'this is actually the best thing Ive ever eaten', @p_output_id);
CALL add_review(5, 1, 'admirably', 'this was my first time every cooking', @p_output_id);

CALL add_review(6, 2, 'splendidly', 'creamy', @p_output_id);
CALL add_review(14, 2, 'muddlingly', 'this would be better without the spinach', @p_output_id);
CALL add_review(3, 2, 'disastrously', 'it took me an hour to chop the garlic', @p_output_id);
CALL add_review(14, 2, 'jubilantly', 'pasta is my favorite', @p_output_id);

CALL add_review(8, 3, 'jubilantly', 'Ive never been so full and satisfied by a meal', @p_output_id);
CALL add_review(12, 3, 'admirably', 'I need to practice grilling more but this was good', @p_output_id);

CALL add_review(12, 4, 'splendidly', 'so sweet', @p_output_id);
CALL add_review(15, 4, 'muddlingly', 'I dont like that the inside of the avocado was referred to as "flesh"', @p_output_id);
CALL add_review(17, 4, 'admirably', 'its not as good as it would be with cream, but avocado makes a good substitute', @p_output_id);
