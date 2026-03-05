/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        school: {
          primary: '#0ea37a',
          'primary-dark': '#0b8a66',
          secondary: '#F7EDE2',
          'secondary-light': '#faf5f0',
          accent: '#0b6b56',
          'accent-alt': '#84a98c',
        },
      },
      keyframes: {
        'fade-in': {
          '0%': { opacity: '0', transform: 'translateY(20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-6px)' },
        },
        'float-delayed': {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-8px)' },
        },
        blob: {
          '0%, 100%': { transform: 'translate(0, 0) scale(1)' },
          '33%': { transform: 'translate(30px, -50px) scale(1.1)' },
          '66%': { transform: 'translate(-20px, 20px) scale(0.9)' },
        },
        'spin-slow': {
          '0%': { transform: 'rotate(0deg)' },
          '100%': { transform: 'rotate(360deg)' },
        },
        'pulse-slow': {
          '0%, 100%': { opacity: '0.65', transform: 'scale(1)' },
          '50%': { opacity: '0.95', transform: 'scale(1.05)' },
        },
      },
      animation: {
        'fade-in': 'fade-in 0.8s ease-out forwards',
        float: 'float 4.2s ease-in-out infinite',
        'float-delayed': 'float-delayed 5s ease-in-out infinite',
        blob: 'blob 7s infinite',
        'spin-slow': 'spin-slow 20s linear infinite',
        'pulse-slow': 'pulse-slow 6.5s ease-in-out infinite',
      },
      animationDelay: {
        '2000': '2s',
        '4000': '4s',
      },
    },
  },
  plugins: [],
};
