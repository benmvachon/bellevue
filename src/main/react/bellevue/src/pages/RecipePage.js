import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import withAuth from '../utils/withAuth.js';
import { getRecipe } from '../api/api.js';

function RecipePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [recipe, setRecipe] = useState(null);

  useEffect(() => {
    getRecipe(id, setRecipe, setRecipe);
  }, [id]);

  const authorClick = (event) => {
    event.preventDefault();
    navigate('/profile/' + event.target.value);
  };

  return (
    <div className="page recipe-page">
      <h1>{recipe?.name}</h1>
      <h2>{recipe?.description}</h2>
      <h3>
        Author:
        <button value={recipe?.author?.id} onClick={authorClick}>
          {recipe?.author?.name}
        </button>
      </h3>
      <h3>Category: {recipe?.category}</h3>
      <h3>Rating: {recipe?.rating}</h3>
      <label htmlFor="vegan">Vegan:</label>
      <input
        type="checkbox"
        id="vegan"
        value="Vegan"
        checked={recipe?.vegan}
        disabled
      />
      <label htmlFor="vegetarian">Vegetarian:</label>
      <input
        type="checkbox"
        id="vegetarian"
        value="Vegetarian"
        checked={recipe?.vegetarian}
        disabled
      />
      <label htmlFor="pescetarian">Pescetarian:</label>
      <input
        type="checkbox"
        id="pescetarian"
        value="Pescetarian"
        checked={recipe?.pescetarian}
        disabled
      />
      <label htmlFor="glutenFree">Gluten Free:</label>
      <input
        type="checkbox"
        id="glutenFree"
        value="Gluten Free"
        checked={recipe?.glutenFree}
        disabled
      />
      <h3>Allergen: {recipe?.allergen}</h3>
      <h3>Ingrediets: </h3>
      {recipe?.ingredients?.map((ingredient) => (
        <ul key={`ingredient-${ingredient.id}`}>
          <li>
            <span>{ingredient.name} </span>
            <span>{ingredient.quantity} </span>
            <span>{ingredient.unit}</span>
          </li>
        </ul>
      ))}
      <h3>Equipment: </h3>
      {recipe?.equipment?.map((equipment) => (
        <ul key={`equipment-${equipment.id}`}>
          <li>{equipment.name}</li>
        </ul>
      ))}
      <h3>Skills: </h3>
      {recipe?.skills?.map((skill) => (
        <ul key={`skill-${skill.id}`}>
          <li>{skill.name}</li>
        </ul>
      ))}
      <h3>Steps: </h3>
      {recipe?.steps?.map((step) => (
        <ul key={`step-${step.order}`}>
          <li>{step.description}</li>
        </ul>
      ))}
    </div>
  );
}

export default withAuth(RecipePage);
