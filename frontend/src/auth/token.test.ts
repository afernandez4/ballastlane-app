import { decodeToken, isExpired } from './token';

/** Builds an unsigned JWT-shaped string. The client only reads the payload, never verifies it. */
function fakeToken(payload: Record<string, unknown>): string {
  const encode = (value: object) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'HS384' })}.${encode(payload)}.signature`;
}

describe('decodeToken', () => {
  it('reads subject, role and expiry from the payload', () => {
    const claims = decodeToken(fakeToken({ sub: 'ash', role: 'ADMIN', exp: 2000000000 }));

    expect(claims).toEqual({ sub: 'ash', role: 'ADMIN', exp: 2000000000 });
  });

  it('returns null for malformed tokens', () => {
    expect(decodeToken('not-a-token')).toBeNull();
    expect(decodeToken(fakeToken({ sub: 'ash' }))).toBeNull();
    expect(decodeToken(fakeToken({ sub: 'ash', role: 'ROOT', exp: 1 }))).toBeNull();
  });
});

describe('isExpired', () => {
  it('is true once the expiry instant has passed', () => {
    const claims = { sub: 'ash', role: 'USER' as const, exp: 1000 };

    expect(isExpired(claims, 999_000)).toBe(false);
    expect(isExpired(claims, 1_000_000)).toBe(true);
  });
});
