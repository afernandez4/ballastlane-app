import { fireEvent, render, screen } from '@testing-library/react';
import { Pagination } from './Pagination';

describe('Pagination', () => {
  it('shows the one-based page and total', () => {
    render(<Pagination page={1} totalPages={5} onPageChange={() => {}} />);

    expect(screen.getByText('Page 2 of 5')).toBeInTheDocument();
  });

  it('disables "Previous" on the first page and "Next" on the last one', () => {
    const { rerender } = render(<Pagination page={0} totalPages={3} onPageChange={() => {}} />);
    expect(screen.getByRole('button', { name: 'Previous' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Next' })).toBeEnabled();

    rerender(<Pagination page={2} totalPages={3} onPageChange={() => {}} />);
    expect(screen.getByRole('button', { name: 'Previous' })).toBeEnabled();
    expect(screen.getByRole('button', { name: 'Next' })).toBeDisabled();
  });

  it('reports the zero-based target page when a button is clicked', () => {
    const onPageChange = vi.fn();
    render(<Pagination page={1} totalPages={4} onPageChange={onPageChange} />);

    fireEvent.click(screen.getByRole('button', { name: 'Next' }));
    fireEvent.click(screen.getByRole('button', { name: 'Previous' }));

    expect(onPageChange).toHaveBeenNthCalledWith(1, 2);
    expect(onPageChange).toHaveBeenNthCalledWith(2, 0);
  });
});
