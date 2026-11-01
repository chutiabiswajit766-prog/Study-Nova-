const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: cannot read another user's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      selectedExam: "JEE Main",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Authenticated user: can create and read their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      selectedExam: "NEET UG",
      createdAt: new Date(),
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can create study materials in their own subcollection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const materialId = "mat_123";
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("materials").doc(materialId).set({
      materialId: materialId,
      userId: ALICE_UID,
      title: "Physics Mechanics Summary",
      topic: "Newton Laws",
      subject: "Physics",
      exam: "JEE Main",
      type: "summary",
      contentJson: "Newton's First Law...",
      source: "ai_generator",
      isFavorite: false,
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot access another user's study materials", async () => {
  const materialId = "mat_bob_99";
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("materials").doc(materialId).set({
      materialId: materialId,
      userId: BOB_UID,
      title: "Bob Secret Notes",
      contentJson: "Secrets",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("materials").doc(materialId).get());
});

test("Authenticated user: can create flashcards in their own subcollection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const cardId = "card_123";
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("flashcards").doc(cardId).set({
      cardId: cardId,
      userId: ALICE_UID,
      deckName: "Chemistry Formulas",
      front: "Boyle's Law formula",
      back: "P1V1 = P2V2 at constant temperature",
      difficulty: "EASY",
      repetitionLevel: 1,
      reviewCount: 0,
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can save mock test result", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const resultId = "mock_res_1";
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("mock_results").doc(resultId).set({
      resultId: resultId,
      userId: ALICE_UID,
      exam: "JEE Main",
      subject: "Physics",
      totalQuestions: 10,
      correctAnswers: 8,
      incorrectAnswers: 2,
      unattempted: 0,
      score: 30,
      maxScore: 40,
      percentage: 75.0,
      accuracy: 80.0,
      timeTakenSeconds: 600,
      createdAt: new Date(),
    })
  );
});
