import { screen } from '@testing-library/react';
import { renderWithProviders } from '../test/renderWithProviders';
import { PokemonCard } from './PokemonCard';

describe('PokemonCard', () => {
  it('shows the name, category, weight and first abilities and links to the detail', () => {
    renderWithProviders(
      <PokemonCard
        id={25}
        name="pikachu"
        spriteUrl="https://img/25.png"
        category="Mouse Pokémon"
        weight={60}
        abilities={[
          { name: 'static', hidden: false },
          { name: 'lightning-rod', hidden: false },
          { name: 'surge-surfer', hidden: true },
        ]}
      />,
    );

    expect(screen.getByRole('link', { name: /Pikachu/ })).toHaveAttribute('href', '/pokedex/25');
    expect(screen.getByText('Mouse Pokémon')).toBeInTheDocument();
    expect(screen.getByText('6.0 kg')).toBeInTheDocument();
    expect(screen.getByText('static, lightning-rod')).toBeInTheDocument();
    expect(screen.getByText('#025')).toBeInTheDocument();
  });

  it('marks the sprite as decorative', () => {
    renderWithProviders(
      <PokemonCard
        id={1}
        name="bulbasaur"
        spriteUrl="https://img/1.png"
        category=""
        weight={69}
        abilities={[]}
      />,
    );

    expect(document.querySelector('img')).toHaveAttribute('alt', '');
  });

  it('shows a text fallback when there is no sprite', () => {
    renderWithProviders(
      <PokemonCard
        id={1}
        name="bulbasaur"
        spriteUrl={null}
        category=""
        weight={69}
        abilities={[]}
      />,
    );

    expect(screen.getByText('No image')).toBeInTheDocument();
  });
});
