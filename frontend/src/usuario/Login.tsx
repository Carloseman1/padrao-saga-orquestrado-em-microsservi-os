import { useNavigate } from 'react-router-dom'
import { FormularioLogin } from './FormularioLogin'
import estilos from './Login.module.css'

export function Login() {
  const navegar = useNavigate()

  return (
    <main className={estilos.pagina}>
      <section className={estilos.cartao}>
        <h1 className={estilos.titulo}>Entrar</h1>
        <FormularioLogin aoEntrar={() => navegar('/')} />
      </section>
    </main>
  )
}
