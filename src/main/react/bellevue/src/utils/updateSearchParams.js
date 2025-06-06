export const updateSearchParams = (updates, searchParams, setSearchParams) => {
  const newParams = new URLSearchParams(searchParams);
  Object.entries(updates).forEach(([key, value]) => {
    if (
      value === undefined ||
      value === null ||
      value === false ||
      (Array.isArray(value) && value.length === 0)
    ) {
      newParams.delete(key);
    } else {
      newParams.set(
        key,
        Array.isArray(value) ? value.join(',') : value.toString()
      );
    }
  });
  setSearchParams(newParams);
};
