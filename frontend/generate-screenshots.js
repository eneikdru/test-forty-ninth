import { chromium } from 'playwright';
import { createServer } from 'vite';
import fs from 'fs';
import path from 'path';

async function generateScreenshots() {
  const rootDir = process.cwd().endsWith('frontend') ? path.resolve(process.cwd(), '..') : process.cwd();
  const targetDir = path.resolve(rootDir, '.eneik/records/design-check-97b8395e-fafa-4c08-91c6-565b8bba690f');
  if (!fs.existsSync(targetDir)) {
    fs.mkdirSync(targetDir, { recursive: true });
  }

  const frontendDir = process.cwd().endsWith('frontend') ? process.cwd() : path.resolve(process.cwd(), 'frontend');
  // Start Vite dev server
  const server = await createServer({
    configFile: path.resolve(frontendDir, 'vite.config.js'),
    server: { port: 5173 }
  });
  await server.listen();
  console.log('Vite server running at http://localhost:5173');

  const browser = await chromium.launch();

  // Desktop 1440px
  const desktopContext = await browser.newContext({
    viewport: { width: 1440, height: 900 }
  });
  const desktopPage = await desktopContext.newPage();
  await desktopPage.goto('http://localhost:5173');
  await desktopPage.waitForSelector('[data-testid="search-input"]');

  const desktopPath = path.join(targetDir, 'desktop-1440.png');
  await desktopPage.screenshot({ path: desktopPath, fullPage: false });
  console.log('Saved desktop screenshot to', desktopPath);

  // Mobile 375px
  const mobileContext = await browser.newContext({
    viewport: { width: 375, height: 812 },
    isMobile: true,
    hasTouch: true,
    deviceScaleFactor: 2
  });
  const mobilePage = await mobileContext.newPage();
  await mobilePage.goto('http://localhost:5173');
  await mobilePage.waitForSelector('[data-testid="search-input"]');

  const mobilePath = path.join(targetDir, 'mobile-375.png');
  await mobilePage.screenshot({ path: mobilePath, fullPage: false });
  console.log('Saved mobile screenshot to', mobilePath);

  await browser.close();
  await server.close();
  console.log('Screenshot generation complete.');
}

generateScreenshots().catch((err) => {
  console.error('Error generating screenshots:', err);
  process.exit(1);
});
