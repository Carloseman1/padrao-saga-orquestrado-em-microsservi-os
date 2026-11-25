import { Outlet, Route, Routes } from 'react-router-dom'
import { Catalogo } from './catalogo/Catalogo'
import { LivroDetalhe } from './catalogo/LivroDetalhe'
import { Cabecalho } from './compartilhado/Cabecalho'
import { Login } from './usuario/Login'

function ComCabecalho() {
  return (
    <>
      <Cabecalho />
      <Outlet />
    </>
  )
}

export default function App() {
  return (
    <Routes>
      <Route element={<ComCabecalho />}>
        <Route path="/" element={<Catalogo />} />
        <Route path="/livros/:codigo" element={<LivroDetalhe />} />
      </Route>
      <Route path="/login" element={<Login />} />
    </Routes>
  )
}
