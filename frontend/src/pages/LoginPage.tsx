import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { login as loginRequest } from '../api/auth';
import { ApiError } from '../api/client';
import { useAuth } from '../auth/useAuth';
import { ErrorState } from '../components/ErrorState';
import { loginSchema, type LoginForm } from '../features/schemas';
import { FieldError, Label } from './formParts';
import { fieldClass } from '../features/formStyles';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from ?? '/my-pokemon';

  const form = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: { username: '', password: '' },
  });

  const mutation = useMutation({
    mutationFn: (values: LoginForm) => loginRequest(values.username, values.password),
    onSuccess: ({ accessToken }) => {
      login(accessToken);
      navigate(from, { replace: true });
    },
  });

  const serverError =
    mutation.error instanceof ApiError && mutation.error.status === 401
      ? 'Wrong username or password.'
      : mutation.error
        ? 'Could not log in. Try again.'
        : null;

  return (
    <section className="mx-auto max-w-md space-y-6">
      <h1 className="text-2xl font-bold tracking-tight">Log in</h1>

      {serverError && <ErrorState message={serverError} />}

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
            aria-describedby={form.formState.errors.username ? 'username-error' : undefined}
            {...form.register('username')}
          />
          <FieldError id="username-error" message={form.formState.errors.username?.message} />
        </div>
        <div>
          <Label htmlFor="password">Password</Label>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            className={fieldClass(!!form.formState.errors.password)}
            aria-invalid={!!form.formState.errors.password}
            aria-describedby={form.formState.errors.password ? 'password-error' : undefined}
            {...form.register('password')}
          />
          <FieldError id="password-error" message={form.formState.errors.password?.message} />
        </div>
        <button
          type="submit"
          disabled={mutation.isPending}
          className="w-full rounded-md bg-slate-900 px-4 py-2 font-medium text-white hover:bg-slate-700 disabled:opacity-60"
        >
          {mutation.isPending ? 'Logging in…' : 'Log in'}
        </button>
      </form>

      <p className="text-sm text-slate-600">
        No account yet?{' '}
        <Link to="/register" className="font-medium text-slate-900 underline">
          Sign up
        </Link>
      </p>
    </section>
  );
}
