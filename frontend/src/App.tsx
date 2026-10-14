import { Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './auth/ProtectedRoute';
import { Layout } from './components/Layout';
import { EditPokemonPage } from './pages/EditPokemonPage';
import { LoginPage } from './pages/LoginPage';
import { MyPokemonPage } from './pages/MyPokemonPage';
import { NotFoundPage } from './pages/NotFoundPage';
import { PokedexPage } from './pages/PokedexPage';
import { PokemonDetailPage } from './pages/PokemonDetailPage';
import { RegisterPage } from './pages/RegisterPage';

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Navigate to="/pokedex" replace />} />
        <Route path="/pokedex" element={<PokedexPage />} />
        <Route path="/pokedex/:idOrName" element={<PokemonDetailPage />} />
        <Route path="/mis-pokemon" element={<MyPokemonPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/mis-pokemon/:id/editar" element={<EditPokemonPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}
