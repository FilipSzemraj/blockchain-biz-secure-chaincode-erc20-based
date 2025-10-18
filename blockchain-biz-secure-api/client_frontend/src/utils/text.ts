/**
 * Formats a camelCase or PascalCase string to a more readable format.
 * - Adds spaces before uppercase letters.
 * - Capitalizes the first letter.
 * @param key - The original camelCase string
 * @returns The formatted string (e.g., "tokenSymbol" → "Token Symbol")
 */
export const formatKey = (key: string): string => {
    return key
        .replace(/([A-Z])/g, " $1") // Insert space before uppercase letters
        .replace(/^./, (char) => char.toUpperCase()); // Capitalize first letter
};