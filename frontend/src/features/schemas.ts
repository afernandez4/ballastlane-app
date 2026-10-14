import { z } from 'zod';

// Rules mirror the backend (Registration and LocalPokemonFields) so most mistakes are caught before a
// request is sent. The server stays the authority: its field errors are shown on the form too.

export const loginSchema = z.object({
  username: z.string().trim().min(1, 'Introduce tu usuario'),
  password: z.string().min(1, 'Introduce tu contraseña'),
});
export type LoginForm = z.infer<typeof loginSchema>;

export const registerSchema = z.object({
  username: z
    .string()
    .trim()
    .regex(/^[A-Za-z0-9_]{3,50}$/, 'Entre 3 y 50 caracteres: letras, números o guion bajo'),
  email: z.string().trim().email('Introduce un correo válido'),
  password: z
    .string()
    .min(8, 'Mínimo 8 caracteres')
    .max(72, 'Máximo 72 caracteres')
    .regex(/[A-Za-z]/, 'Debe incluir al menos una letra')
    .regex(/\d/, 'Debe incluir al menos un número'),
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
      .min(1, 'El nombre local es obligatorio')
      .max(100, 'Máximo 100 caracteres'),
    region: z.string().trim().max(100, 'Máximo 100 caracteres'),
    tagsText: z.string(),
  })
  .superRefine((value, ctx) => {
    const tags = parseTags(value.tagsText);
    if (tags.length > 10) {
      ctx.addIssue({ code: 'custom', path: ['tagsText'], message: 'Máximo 10 etiquetas' });
    }
    if (tags.some((tag) => tag.length > 30)) {
      ctx.addIssue({
        code: 'custom',
        path: ['tagsText'],
        message: 'Cada etiqueta tiene como máximo 30 caracteres',
      });
    }
    if (new Set(tags).size !== tags.length) {
      ctx.addIssue({ code: 'custom', path: ['tagsText'], message: 'No repitas etiquetas' });
    }
  });
export type LocalPokemonForm = z.infer<typeof localPokemonSchema>;
