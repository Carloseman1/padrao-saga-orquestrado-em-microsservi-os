import { Route, Routes } from 'react-router-dom'
import { Login } from './usuario/Login'
import { useSessao } from './usuario/useSessao'

function Inicio() {
  const { usuario } = useSessao()
  return <h1>{usuario ? `Olá, ${usuario.nome}` : 'Livraria'}</h1>
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Inicio />} />
      <Route path="/login" element={<Login />} />
    </Routes>
  )
}