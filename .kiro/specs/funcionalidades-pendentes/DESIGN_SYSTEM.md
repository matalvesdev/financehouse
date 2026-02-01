# Design System - Gestão Financeira Doméstica

## 🎨 Visão Geral

Design system minimalista e moderno inspirado nas melhores práticas do Dribbble e Behance para aplicações financeiras. Focado em clareza, confiança e usabilidade.

## 🎯 Princípios de Design

1. **Minimalismo** - Menos é mais. Cada elemento tem um propósito.
2. **Clareza** - Informação financeira deve ser clara e fácil de entender.
3. **Confiança** - Design profissional que transmite segurança.
4. **Modernidade** - Interface contemporânea e agradável.
5. **Acessibilidade** - Contraste adequado e legibilidade em todos os dispositivos.

---

## 📝 Tipografia

### Fonte Principal: DM Sans

**DM Sans** é uma fonte geométrica sans-serif moderna, perfeita para interfaces minimalistas. Oferece excelente legibilidade em telas e transmite profissionalismo.

```css
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&display=swap');

font-family: 'DM Sans', sans-serif;
```

### Hierarquia Tipográfica

```css
/* Display - Títulos principais */
.text-display {
  font-size: 3.5rem;      /* 56px */
  font-weight: 700;
  line-height: 1.1;
  letter-spacing: -0.02em;
}

/* H1 - Títulos de página */
.text-h1 {
  font-size: 2.5rem;      /* 40px */
  font-weight: 700;
  line-height: 1.2;
  letter-spacing: -0.01em;
}

/* H2 - Títulos de seção */
.text-h2 {
  font-size: 2rem;        /* 32px */
  font-weight: 600;
  line-height: 1.25;
}

/* H3 - Subtítulos */
.text-h3 {
  font-size: 1.5rem;      /* 24px */
  font-weight: 600;
  line-height: 1.3;
}

/* H4 - Títulos de card */
.text-h4 {
  font-size: 1.25rem;     /* 20px */
  font-weight: 600;
  line-height: 1.4;
}

/* Body Large - Texto destacado */
.text-body-lg {
  font-size: 1.125rem;    /* 18px */
  font-weight: 400;
  line-height: 1.6;
}

/* Body - Texto padrão */
.text-body {
  font-size: 1rem;        /* 16px */
  font-weight: 400;
  line-height: 1.5;
}

/* Body Small - Texto secundário */
.text-body-sm {
  font-size: 0.875rem;    /* 14px */
  font-weight: 400;
  line-height: 1.5;
}

/* Caption - Legendas e labels */
.text-caption {
  font-size: 0.75rem;     /* 12px */
  font-weight: 500;
  line-height: 1.4;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

/* Numbers - Valores monetários */
.text-number {
  font-size: 2rem;        /* 32px */
  font-weight: 700;
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}
```

### Pesos de Fonte

- **Regular (400)**: Texto corpo, descrições
- **Medium (500)**: Labels, captions
- **Semi-Bold (600)**: Subtítulos, botões
- **Bold (700)**: Títulos, valores monetários

---

## 🎨 Paleta de Cores

### Cores Primárias

```css
/* Verde Principal - Crescimento, Prosperidade */
--primary-50:  #E8F5E9;   /* Muito claro - backgrounds */
--primary-100: #C8E6C9;   /* Claro - hover states */
--primary-200: #A5D6A7;   /* Médio claro */
--primary-300: #81C784;   /* Médio */
--primary-400: #66BB6A;   /* Médio escuro */
--primary-500: #4CAF50;   /* Principal - botões, links */
--primary-600: #43A047;   /* Escuro - hover */
--primary-700: #388E3C;   /* Muito escuro */
--primary-800: #2E7D32;   /* Extra escuro */
--primary-900: #1B5E20;   /* Mais escuro */

/* Verde Escuro - Confiança, Estabilidade */
--green-dark-50:  #E8F4F0;
--green-dark-100: #C1E3D6;
--green-dark-200: #96D1BA;
--green-dark-300: #6BBF9E;
--green-dark-400: #4BB189;
--green-dark-500: #2BA374;   /* Secundário */
--green-dark-600: #269B6C;
--green-dark-700: #1F9161;
--green-dark-800: #198757;
--green-dark-900: #0F7544;
```

### Cores Neutras

```css
/* Preto e Cinzas */
--neutral-0:   #FFFFFF;   /* Branco puro */
--neutral-50:  #FAFAFA;   /* Quase branco - backgrounds */
--neutral-100: #F5F5F5;   /* Cinza muito claro */
--neutral-200: #EEEEEE;   /* Cinza claro - borders */
--neutral-300: #E0E0E0;   /* Cinza médio claro */
--neutral-400: #BDBDBD;   /* Cinza médio */
--neutral-500: #9E9E9E;   /* Cinza - texto secundário */
--neutral-600: #757575;   /* Cinza escuro */
--neutral-700: #616161;   /* Cinza muito escuro */
--neutral-800: #424242;   /* Quase preto */
--neutral-900: #212121;   /* Preto suave */
--neutral-950: #0A0A0A;   /* Preto profundo */
```

### Cores de Status

```css
/* Verde - Sucesso, Receita, Positivo */
--success-50:  #E8F5E9;
--success-100: #C8E6C9;
--success-500: #4CAF50;   /* Principal */
--success-600: #43A047;
--success-700: #388E3C;

/* Laranja - Atenção, Alerta, Próximo do Limite */
--warning-50:  #FFF3E0;
--warning-100: #FFE0B2;
--warning-500: #FF9800;   /* Principal */
--warning-600: #FB8C00;
--warning-700: #F57C00;

/* Vermelho - Erro, Despesa, Negativo, Excedido */
--error-50:  #FFEBEE;
--error-100: #FFCDD2;
--error-500: #F44336;     /* Principal */
--error-600: #E53935;
--error-700: #D32F2F;

/* Azul - Informação (uso secundário) */
--info-50:  #E3F2FD;
--info-100: #BBDEFB;
--info-500: #2196F3;
--info-600: #1E88E5;
--info-700: #1976D2;
```

### Aplicação das Cores

```css
/* Backgrounds */
--bg-primary: var(--neutral-0);        /* Branco */
--bg-secondary: var(--neutral-50);     /* Cinza muito claro */
--bg-tertiary: var(--neutral-100);     /* Cinza claro */
--bg-dark: var(--neutral-900);         /* Preto suave */

/* Texto */
--text-primary: var(--neutral-900);    /* Preto suave */
--text-secondary: var(--neutral-600);  /* Cinza escuro */
--text-tertiary: var(--neutral-500);   /* Cinza */
--text-inverse: var(--neutral-0);      /* Branco */
--text-success: var(--success-700);    /* Verde escuro */
--text-warning: var(--warning-700);    /* Laranja escuro */
--text-error: var(--error-700);        /* Vermelho escuro */

/* Borders */
--border-light: var(--neutral-200);    /* Cinza claro */
--border-medium: var(--neutral-300);   /* Cinza médio claro */
--border-dark: var(--neutral-400);     /* Cinza médio */
```

---

## 📐 Espaçamento

Sistema de espaçamento baseado em múltiplos de 4px para consistência.

```css
--space-1:  0.25rem;  /* 4px */
--space-2:  0.5rem;   /* 8px */
--space-3:  0.75rem;  /* 12px */
--space-4:  1rem;     /* 16px */
--space-5:  1.25rem;  /* 20px */
--space-6:  1.5rem;   /* 24px */
--space-8:  2rem;     /* 32px */
--space-10: 2.5rem;   /* 40px */
--space-12: 3rem;     /* 48px */
--space-16: 4rem;     /* 64px */
--space-20: 5rem;     /* 80px */
--space-24: 6rem;     /* 96px */
```

### Uso Recomendado

- **4px (space-1)**: Espaçamento mínimo entre elementos relacionados
- **8px (space-2)**: Padding interno de badges, tags
- **12px (space-3)**: Gap entre ícone e texto
- **16px (space-4)**: Padding padrão de botões, inputs
- **24px (space-6)**: Gap entre cards, seções
- **32px (space-8)**: Padding de containers
- **48px (space-12)**: Margem entre seções principais

---

## 🔲 Componentes

### Botões

```css
/* Botão Primary */
.btn-primary {
  background: var(--primary-500);
  color: var(--neutral-0);
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.2s ease;
}

.btn-primary:hover {
  background: var(--primary-600);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

/* Botão Secondary */
.btn-secondary {
  background: transparent;
  color: var(--primary-500);
  border: 2px solid var(--primary-500);
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.2s ease;
}

.btn-secondary:hover {
  background: var(--primary-50);
  border-color: var(--primary-600);
  color: var(--primary-600);
}

/* Botão Ghost */
.btn-ghost {
  background: transparent;
  color: var(--neutral-700);
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 500;
  font-size: 1rem;
  transition: all 0.2s ease;
}

.btn-ghost:hover {
  background: var(--neutral-100);
  color: var(--neutral-900);
}

/* Botão Danger */
.btn-danger {
  background: var(--error-500);
  color: var(--neutral-0);
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.2s ease;
}

.btn-danger:hover {
  background: var(--error-600);
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.3);
}
```

### Cards

```css
/* Card Base */
.card {
  background: var(--neutral-0);
  border-radius: 1rem;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid var(--neutral-200);
  transition: all 0.2s ease;
}

.card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: var(--neutral-300);
}

/* Card com destaque */
.card-highlight {
  background: linear-gradient(135deg, var(--primary-50) 0%, var(--neutral-0) 100%);
  border: 2px solid var(--primary-200);
}

/* Card de métrica */
.card-metric {
  background: var(--neutral-0);
  border-radius: 1rem;
  padding: 2rem;
  border-left: 4px solid var(--primary-500);
}
```

### Inputs

```css
/* Input Base */
.input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid var(--neutral-300);
  border-radius: 0.5rem;
  font-size: 1rem;
  font-family: 'DM Sans', sans-serif;
  transition: all 0.2s ease;
  background: var(--neutral-0);
  color: var(--neutral-900);
}

.input:focus {
  outline: none;
  border-color: var(--primary-500);
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
}

.input::placeholder {
  color: var(--neutral-500);
}

/* Input com erro */
.input-error {
  border-color: var(--error-500);
}

.input-error:focus {
  box-shadow: 0 0 0 3px rgba(244, 67, 54, 0.1);
}
```

### Badges

```css
/* Badge Success */
.badge-success {
  background: var(--success-100);
  color: var(--success-700);
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* Badge Warning */
.badge-warning {
  background: var(--warning-100);
  color: var(--warning-700);
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* Badge Error */
.badge-error {
  background: var(--error-100);
  color: var(--error-700);
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* Badge Neutral */
.badge-neutral {
  background: var(--neutral-200);
  color: var(--neutral-700);
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
```

---

## 🎭 Sombras

```css
/* Sombras sutis para minimalismo */
--shadow-xs:  0 1px 2px rgba(0, 0, 0, 0.05);
--shadow-sm:  0 1px 3px rgba(0, 0, 0, 0.08);
--shadow-md:  0 4px 6px rgba(0, 0, 0, 0.07);
--shadow-lg:  0 10px 15px rgba(0, 0, 0, 0.08);
--shadow-xl:  0 20px 25px rgba(0, 0, 0, 0.1);
--shadow-2xl: 0 25px 50px rgba(0, 0, 0, 0.12);

/* Sombra com cor primária */
--shadow-primary: 0 4px 12px rgba(76, 175, 80, 0.2);
```

---

## 📏 Border Radius

```css
--radius-sm:   0.25rem;  /* 4px - badges, tags */
--radius-md:   0.5rem;   /* 8px - botões, inputs */
--radius-lg:   0.75rem;  /* 12px - cards pequenos */
--radius-xl:   1rem;     /* 16px - cards grandes */
--radius-2xl:  1.5rem;   /* 24px - containers */
--radius-full: 9999px;   /* Circular - avatares, badges */
```

---

## 🎬 Animações

```css
/* Transições suaves */
--transition-fast:   150ms ease;
--transition-base:   200ms ease;
--transition-slow:   300ms ease;

/* Easing curves */
--ease-in:     cubic-bezier(0.4, 0, 1, 1);
--ease-out:    cubic-bezier(0, 0, 0.2, 1);
--ease-in-out: cubic-bezier(0.4, 0, 0.2, 1);

/* Animações de entrada */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
```

---

## 📱 Breakpoints

```css
/* Mobile First */
--screen-sm:  640px;   /* Tablet pequeno */
--screen-md:  768px;   /* Tablet */
--screen-lg:  1024px;  /* Desktop */
--screen-xl:  1280px;  /* Desktop grande */
--screen-2xl: 1536px;  /* Desktop extra grande */
```

---

## 🎨 Configuração TailwindCSS

```javascript
// tailwind.config.js
module.exports = {
  content: ['./src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['DM Sans', 'sans-serif'],
      },
      colors: {
        primary: {
          50: '#E8F5E9',
          100: '#C8E6C9',
          200: '#A5D6A7',
          300: '#81C784',
          400: '#66BB6A',
          500: '#4CAF50',
          600: '#43A047',
          700: '#388E3C',
          800: '#2E7D32',
          900: '#1B5E20',
        },
        'green-dark': {
          50: '#E8F4F0',
          100: '#C1E3D6',
          200: '#96D1BA',
          300: '#6BBF9E',
          400: '#4BB189',
          500: '#2BA374',
          600: '#269B6C',
          700: '#1F9161',
          800: '#198757',
          900: '#0F7544',
        },
        neutral: {
          0: '#FFFFFF',
          50: '#FAFAFA',
          100: '#F5F5F5',
          200: '#EEEEEE',
          300: '#E0E0E0',
          400: '#BDBDBD',
          500: '#9E9E9E',
          600: '#757575',
          700: '#616161',
          800: '#424242',
          900: '#212121',
          950: '#0A0A0A',
        },
        success: {
          50: '#E8F5E9',
          100: '#C8E6C9',
          500: '#4CAF50',
          600: '#43A047',
          700: '#388E3C',
        },
        warning: {
          50: '#FFF3E0',
          100: '#FFE0B2',
          500: '#FF9800',
          600: '#FB8C00',
          700: '#F57C00',
        },
        error: {
          50: '#FFEBEE',
          100: '#FFCDD2',
          500: '#F44336',
          600: '#E53935',
          700: '#D32F2F',
        },
      },
      spacing: {
        '18': '4.5rem',
        '88': '22rem',
        '128': '32rem',
      },
      borderRadius: {
        '4xl': '2rem',
      },
      boxShadow: {
        'primary': '0 4px 12px rgba(76, 175, 80, 0.2)',
      },
    },
  },
  plugins: [require('daisyui')],
  daisyui: {
    themes: [
      {
        financehouse: {
          'primary': '#4CAF50',
          'secondary': '#2BA374',
          'accent': '#66BB6A',
          'neutral': '#212121',
          'base-100': '#FFFFFF',
          'base-200': '#FAFAFA',
          'base-300': '#F5F5F5',
          'info': '#2196F3',
          'success': '#4CAF50',
          'warning': '#FF9800',
          'error': '#F44336',
        },
      },
    ],
  },
}
```

---

## 🎯 Exemplos de Uso

### Dashboard Card

```tsx
<div className="card bg-white rounded-xl p-6 border border-neutral-200 hover:shadow-lg transition-all">
  <div className="flex items-center justify-between mb-4">
    <h3 className="text-h4 text-neutral-900">Saldo Atual</h3>
    <span className="badge-success">+12%</span>
  </div>
  <p className="text-number text-primary-600">R$ 5.420,00</p>
  <p className="text-body-sm text-neutral-600 mt-2">
    Comparado ao mês anterior
  </p>
</div>
```

### Budget Progress Card

```tsx
<div className="card bg-white rounded-xl p-6 border-l-4 border-primary-500">
  <div className="flex justify-between items-start mb-3">
    <h4 className="text-h4 text-neutral-900">Alimentação</h4>
    <span className="badge-warning">70%</span>
  </div>
  
  <div className="w-full bg-neutral-200 rounded-full h-2 mb-3">
    <div 
      className="bg-primary-500 h-2 rounded-full transition-all"
      style={{ width: '70%' }}
    />
  </div>
  
  <div className="flex justify-between text-body-sm">
    <span className="text-neutral-600">R$ 350,00</span>
    <span className="text-neutral-900 font-semibold">R$ 500,00</span>
  </div>
</div>
```

### Transaction Item

```tsx
<div className="flex items-center justify-between p-4 bg-white rounded-lg border border-neutral-200 hover:bg-neutral-50 transition-colors">
  <div className="flex items-center gap-4">
    <div className="w-10 h-10 rounded-full bg-primary-100 flex items-center justify-center">
      <ShoppingCart className="w-5 h-5 text-primary-600" />
    </div>
    <div>
      <p className="text-body font-semibold text-neutral-900">
        Supermercado
      </p>
      <p className="text-body-sm text-neutral-600">
        15 Jan 2026
      </p>
    </div>
  </div>
  <span className="text-body font-semibold text-error-600">
    - R$ 150,00
  </span>
</div>
```

---

## ✅ Checklist de Implementação

- [ ] Instalar fonte DM Sans do Google Fonts
- [ ] Configurar paleta de cores no Tailwind
- [ ] Configurar tema customizado no DaisyUI
- [ ] Criar componentes base (Button, Card, Input, Badge)
- [ ] Implementar sistema de espaçamento consistente
- [ ] Adicionar animações sutis
- [ ] Testar contraste de cores (WCAG AA)
- [ ] Validar responsividade em todos os breakpoints
- [ ] Documentar componentes no Storybook (opcional)

---

**Design System inspirado nas melhores práticas de Dribbble e Behance** 🎨
