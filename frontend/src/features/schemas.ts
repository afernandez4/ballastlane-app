import { z } from 'zod';

// Rules mirror the backend (Registration and LocalPokemonFields) so most mistakes are caught before a
// request is sent. The server stays the authority: its field errors are shown on the form too.

export const loginSchema = z.object({
  username: z.string().trim().min(1, 'Enter your username'),
  password: z.string().min(1, 'Enter your password'),
});
export type LoginForm = z.infer<typeof loginSchema>;

export const registerSchema = z.object({
  username: z
    .string()
    .trim()
    .regex(/^[A-Za-z0-9_]{3,50}$/, '3 to 50 characters: letters, digits or underscores'),
  email: z.string().trim().email('Enter a valid email address'),
  password: z
    .string()
    .min(8, 'At least 8 characters')
    .max(72, 'At most 72 characters')
    .regex(/[A-Za-z]/, 'Must include at least one letter')
    .regex(/\d/, 'Must include at least one digit'),
});
export type RegisterForm = z.infer<typeof registerSchema>;

/** Tags are typed as "fire, starter" and split on commas before validation. */
export function parseTags(text: string): string[] {
  return text
    .split(',')
    .map((tag) => tag.trim().toLowerCase())
    .filter((tag) => tag.length > 0);
}

export const localPokemonSchema = z
  .object({
    localName: z
      .string()
      .trim()
      .min(1, 'Local name is required')
      .max(100, 'At most 100 characters'),
    region: z.string().trim().max(100, 'At most 100 characters'),
    tagsText: z.string(),
  })
  .superRefine((value, ctx) => {
    const tags = parseTags(value.tagsText);
    if (tags.length > 10) {
      ctx.addIssue({ code: 'custom', path: ['tagsText'], message: 'At most 10 tags' });
    }
    if (tags.some((tag) => tag.length > 30)) {
      ctx.addIssue({
        code: 'custom',
        path: ['tagsText'],
        message: 'Each tag can have at most 30 characters',
      });
    }
    if (new Set(tags).size !== tags.length) {
      ctx.addIssue({ code: 'custom', path: ['tagsText'], message: 'Do not repeat tags' });
    }
  });
export type LocalPokemonForm = z.infer<typeof localPokemonSchema>;
