/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      backgroundColor: {
        primary: 'var(--bg-primary)',
        secondary: 'var(--bg-secondary)',
        card: 'var(--bg-card)',
      },
      textColor: {
        primary: 'var(--text-primary)',
        secondary: 'var(--text-secondary)',
        muted: 'var(--text-muted)',
      },
      borderColor: {
        'border-color': 'var(--border-color)',
      },
      backgroundImage: {
        'gradient-blue': 'var(--gradient-blue)',
        'gradient-green': 'var(--gradient-green)',
      },
      boxShadow: {
        glow: 'var(--shadow-glow)',
      },
    },
  },
  plugins: [],
}
