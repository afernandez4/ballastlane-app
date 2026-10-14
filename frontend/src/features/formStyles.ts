/** Input classes shared by the forms. Invalid inputs get a red border. */
export function fieldClass(invalid: boolean): string {
  return `mt-1 w-full rounded-md border px-3 py-2 text-slate-900 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-slate-700 ${
    invalid ? 'border-red-600' : 'border-slate-300'
  }`;
}
