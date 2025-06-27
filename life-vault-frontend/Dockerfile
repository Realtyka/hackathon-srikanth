FROM node:18-alpine as builder

WORKDIR /app

# Copy package files
COPY package.json ./
RUN npm install

# Copy source code
COPY . .

# Build the app in development mode for local Docker
ARG NODE_ENV=development
ENV NODE_ENV=${NODE_ENV}
RUN npm run build -- --mode development

# Production stage
FROM node:18-alpine

WORKDIR /app

# Install serve to run the app
RUN npm install -g serve

# Copy built files
COPY --from=builder /app/dist ./dist

EXPOSE 3000

CMD ["serve", "-s", "dist", "-l", "3000"]