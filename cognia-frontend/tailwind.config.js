/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'dark-bg': '#0a0e1a',
        'dark-card': '#111827',
        'dark-border': '#1f2937',
        'primary': '#6366f1',
        'primary-light': '#818cf8',
        'accent-purple': '#a855f7',
        'accent-pink': '#ec4899',
        'accent-blue': '#3b82f6',
        'accent-cyan': '#06b6d4',
        'text-primary': '#f9fafb',
        'text-secondary': '#9ca3af',
        'text-muted': '#6b7280',
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'gradient-primary': 'linear-gradient(135deg, #6366f1 0%, #a855f7 50%, #ec4899 100%)',
        'gradient-card': 'linear-gradient(180deg, rgba(99, 102, 241, 0.1) 0%, rgba(17, 24, 39, 0) 100%)',
      },
      boxShadow: {
        'glow': '0 0 20px rgba(99, 102, 241, 0.3)',
        'glow-lg': '0 0 40px rgba(99, 102, 241, 0.4)',
      },
    },
  },
  plugins: [],
}
