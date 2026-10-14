import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { register as registerRequest } from '../api/auth';
import { ApiError } from '../api/client';
import { ErrorState } from '../components/ErrorState';
import { useToast } from '../components/useToast';
import { registerSchema, type RegisterForm } from '../features/schemas';
import { FieldError, Label } from './formParts';
import { fieldClass } from '../features/formStyles';

const FIELDS: (keyof RegisterForm)[] = ['username', 'email', 'password'];

export function RegisterPage() {
  const navigate = useNavigate();
  const toast = useToast();

  const form = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
    defaultValues: { username: '', email: '', password: '' },
  });

  const mutation = useMutation({
    mutationFn: (values: RegisterForm) => registerRequest(values),
    onSuccess: () => {
      toast.show('Account created. You can now log in.');
      navigate('/login', { replace: true });
    },
    onError: (error) => {
      if (!(error instanceof ApiError)) return;
      // Show the server's field messages next to the inputs that caused them.
      for (const field of FIELDS) {
        const message = error.fieldErrors[field];
        if (message) form.setError(field, { type: 'server', message });
      }
    },
  });

  const serverMessage =
    mutation.error instanceof ApiError && mutation.error.status === 409
      ? 'That username or email is already registered.'
      : mutation.error && !(mutation.error instanceof ApiError && mutation.error.status === 400)
        ? 'Could not create the account. Try again.'
        : null;

  return (
    <section className="mx-auto max-w-md space-y-6">
      <h1 className="text-2xl font-bold tracking-tight">Create account</h1>

      {serverMessage && <ErrorState message={serverMessage} />}

      <form
        noValidate
        onSubmit={form.handleSubmit((values) => mutation.mutate(values))}
        className="space-y-4 rounded-xl bg-white p-6 shadow-sm"
      >
        <div>
          <Label htmlFor="username">Username</Label>
          <input
            id="username"
            autoComplete="username"
            className={fieldClass(!!form.formState.errors.username)}
            aria-invalid={!!form.formState.errors.username}
            aria-describedby="username-help username-error"
            {...form.register('username')}
          />
          <p id="username-help" className="text-xs text-slate-500">
            Letters, digits or underscores. 3 to 50 characters.
          </p>
          <FieldError id="username-error" message={form.formState.errors.username?.message} />
        </div>
        <div>
          <Label htmlFor="email">Email</Label>
          <input
            id="email"
            type="email"
            autoComplete="email"
            className={fieldClass(!!form.formState.errors.email)}
            aria-invalid={!!form.formState.errors.email}
            aria-describedby="email-error"
            {...form.register('email')}
          />
          <FieldError id="email-error" message={form.formState.errors.email?.message} />
        </div>
        <div>
          <Label htmlFor="password">Password</Label>
          <input
            id="password"
            type="password"
            autoComplete="new-password"
            className={fieldClass(!!form.formState.errors.password)}
            aria-invalid={!!form.formState.errors.password}
            aria-describedby="password-help password-error"
            {...form.register('password')}
          />
          <p id="password-help" className="text-xs text-slate-500">
            At least 8 characters, with at least one letter and one digit.
          </p>
          <FieldError id="password-error" message={form.formState.errors.password?.message} />
        </div>
        <button
          type="submit"
          disabled={mutation.isPending}
          className="w-full rounded-md bg-slate-900 px-4 py-2 font-medium text-white hover:bg-slate-700 disabled:opacity-60"
        >
          {mutation.isPending ? 'Creating…' : 'Create account'}
        </button>
      </form>

      <p className="text-sm text-slate-600">
        Already have an account?{' '}
        <Link to="/login" className="font-medium text-slate-900 underline">
          Log in
        </Link>
      </p>
    </section>
  );
}
