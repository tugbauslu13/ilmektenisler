const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
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

function validSavedRecipe(uid) {
  const now = new Date();
  return {
    userId: uid,
    title: "Papatya Motifli Hırka",
    sourceType: "ai",
    category: "Giyim & Hırka",
    difficulty: "Orta",
    yarnType: "Pamuklu DK (3 numara)",
    hookSize: "3.5 mm tığ",
    estimatedDuration: "12 saat",
    abbreviationsNote: "zn: zincir, x: sık iğne, v: artırma, a: eksiltme",
    steps: ["1. Tur: Sihirli halka içine 6x (6)", "2. Tur: Her ilmeğe v (12)"],
    imageKey: "img_crochet_cardigan",
    authorName: "İlmekten İşler AI",
    isFavorite: false,
    createdAt: now,
    updatedAt: now,
  };
}

function validCommunityPost(uid) {
  const now = new Date();
  return {
    authorId: uid,
    authorName: "Ayşe Örgücü",
    authorPhotoUrl: "https://example.com/avatar.jpg",
    title: "Bahar Çantası",
    category: "Çanta",
    difficulty: "Başlangıç",
    yarnType: "Kağıt İp",
    yarnBrand: "Spagettiyarn",
    hookSize: "4.5 mm tığ",
    recipeNote: "Tabanı sıkı örün.",
    steps: ["1. Sıra: 20 zn çekin", "2. Sıra: Her zincire x"],
    imageKey: "img_knit_tote",
    imageUrl: "https://example.com/bag.jpg",
    likesCount: 0,
    commentsCount: 0,
    isPublished: true,
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user: cannot list savedRecipes, communityPosts, or comments", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("savedRecipes").get());
  await assertFails(unauthDb.collection("communityPosts").where("isPublished", "==", true).get());
  await assertFails(unauthDb.collection("communityPosts").doc("post_1").collection("comments").where("isPublished", "==", true).get());
});

test("Authenticated user: can create, read, favorite-update, and delete own savedRecipe", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const recipeRef = aliceDb.collection("users").doc(ALICE_UID).collection("savedRecipes").doc("rec_1");
  await assertSucceeds(recipeRef.set(validSavedRecipe(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("savedRecipes").get());
  await assertSucceeds(recipeRef.update({ isFavorite: true, updatedAt: new Date() }));
  await assertSucceeds(recipeRef.delete());
});

test("Authenticated user: cannot read or write another user's savedRecipes or likedPosts", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(BOB_UID).collection("savedRecipes").doc("bob_rec").set({
      userId: BOB_UID,
      title: "Bob Tavşanı",
      sourceType: "ai",
      category: "Amigurumi",
      difficulty: "Başlangıç",
      yarnType: "Kadife İp",
      hookSize: "4.0 mm tığ",
      steps: ["1. Tur: 6x"],
      isFavorite: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("savedRecipes").doc("bob_rec").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("savedRecipes").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("likedPosts").doc("p1").set({
    userId: ALICE_UID,
    postId: "p1",
    createdAt: new Date(),
  }));
});

test("Shadow update with ghost field fails on savedRecipes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const badPayload = { ...validSavedRecipe(ALICE_UID), isAdminVerified: true };
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).collection("savedRecipes").doc("rec_shadow").set(badPayload));
});

test("Community posts: unfiltered list fails, published list succeeds, like delta +1 succeeds, invalid +5 fails, cross-user delete fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const postRef = aliceDb.collection("communityPosts").doc("post_1");
  await assertSucceeds(postRef.set(validCommunityPost(ALICE_UID)));

  // Unfiltered list fails (rules are not filters)
  await assertFails(bobDb.collection("communityPosts").get());
  // Filtered published query succeeds
  await assertSucceeds(bobDb.collection("communityPosts").where("isPublished", "==", true).get());
  // Bob can increment likesCount by +1
  await assertSucceeds(bobDb.collection("communityPosts").doc("post_1").update({ likesCount: 1, updatedAt: new Date() }));
  // Bob cannot jump likesCount by +5
  await assertFails(bobDb.collection("communityPosts").doc("post_1").update({ likesCount: 6, updatedAt: new Date() }));
  // Cross-user title update and delete fail
  await assertFails(bobDb.collection("communityPosts").doc("post_1").update({ title: "Hacked", updatedAt: new Date() }));
  await assertFails(bobDb.collection("communityPosts").doc("post_1").delete());
});

test("Community post comments: published comments readable, author can create/delete own comment, cross-user delete or spoofing fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("communityPosts").doc("post_1").set(validCommunityPost(ALICE_UID)));

  const commentRef = bobDb.collection("communityPosts").doc("post_1").collection("comments").doc("c_1");
  await assertSucceeds(commentRef.set({
    postId: "post_1",
    authorId: BOB_UID,
    authorName: "Bob Örgü",
    authorPhotoUrl: "",
    text: "Hangi marka ip kullandınız?",
    isPublished: true,
    createdAt: new Date(),
  }));

  // Filtered query succeeds, unfiltered fails
  await assertSucceeds(aliceDb.collection("communityPosts").doc("post_1").collection("comments").where("isPublished", "==", true).get());
  await assertFails(aliceDb.collection("communityPosts").doc("post_1").collection("comments").get());

  // Spoofed authorId fails
  await assertFails(aliceDb.collection("communityPosts").doc("post_1").collection("comments").doc("c_spoof").set({
    postId: "post_1",
    authorId: BOB_UID,
    authorName: "Fake",
    text: "Spoof",
    isPublished: true,
    createdAt: new Date(),
  }));

  // Cross-user delete fails, owner delete succeeds
  await assertFails(aliceDb.collection("communityPosts").doc("post_1").collection("comments").doc("c_1").delete());
  await assertSucceeds(commentRef.delete());
});

test("Social posts (Twitter-like feed): unauth blocked, unfiltered list blocked, publish/like/reply/delete enforced", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertFails(unauthDb.collection("socialPosts").where("isPublished", "==", true).get());

  const socialRef = aliceDb.collection("socialPosts").doc("sp_1");
  await assertSucceeds(socialRef.set({
    authorId: ALICE_UID,
    authorName: "Ayşe Örgücü",
    authorHandle: "@ayseorgu",
    authorPhotoUrl: "",
    topic: "Örgü Günlüğü",
    text: "Pazar kahvesi eşliğinde hırka motiflerini birleştiriyorum!",
    likesCount: 0,
    repliesCount: 0,
    isPublished: true,
    createdAt: new Date(),
    updatedAt: new Date(),
  }));

  // Unfiltered list blocked, filtered list succeeds
  await assertFails(bobDb.collection("socialPosts").get());
  await assertSucceeds(bobDb.collection("socialPosts").where("isPublished", "==", true).get());

  // Bob likes social post and increments likesCount by +1
  await assertSucceeds(bobDb.collection("users").doc(BOB_UID).collection("likedSocialPosts").doc("sp_1").set({
    userId: BOB_UID,
    postId: "sp_1",
    createdAt: new Date(),
  }));
  await assertSucceeds(socialRef.update({ likesCount: 1, updatedAt: new Date() }));

  // Bob cannot jump likesCount by +10 or edit Alice's text
  await assertFails(bobDb.collection("socialPosts").doc("sp_1").update({ likesCount: 11, updatedAt: new Date() }));
  await assertFails(bobDb.collection("socialPosts").doc("sp_1").update({ text: "Değiştirildi", updatedAt: new Date() }));

  // Bob replies and increments repliesCount by +1
  const replyRef = bobDb.collection("socialPosts").doc("sp_1").collection("replies").doc("r_1");
  await assertSucceeds(replyRef.set({
    postId: "sp_1",
    authorId: BOB_UID,
    authorName: "Bob Örgü",
    authorHandle: "@boborgu",
    authorPhotoUrl: "",
    text: "Kolay gelsin! Hangi tığ numarasını kullanıyorsun?",
    isPublished: true,
    createdAt: new Date(),
  }));
  await assertSucceeds(bobDb.collection("socialPosts").doc("sp_1").update({ repliesCount: 1, updatedAt: new Date() }));
  await assertSucceeds(aliceDb.collection("socialPosts").doc("sp_1").collection("replies").where("isPublished", "==", true).get());

  // Alice cannot delete Bob's reply; Bob can delete own reply; Bob cannot delete Alice's post
  await assertFails(aliceDb.collection("socialPosts").doc("sp_1").collection("replies").doc("r_1").delete());
  await assertSucceeds(replyRef.delete());
  await assertFails(bobDb.collection("socialPosts").doc("sp_1").delete());
  await assertSucceeds(socialRef.delete());
});

test("Community makes ('Ben de Ördüm!'): unauth blocked, unfiltered list blocked, create/read/delete enforced", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertFails(unauthDb.collection("communityPosts").doc("disc_1").collection("makes").where("isPublished", "==", true).get());

  const makeRef = bobDb.collection("communityPosts").doc("disc_1").collection("makes").doc("mk_1");
  await assertSucceeds(makeRef.set({
    postId: "disc_1",
    authorId: BOB_UID,
    authorName: "Bob Örgücü",
    authorPhotoUrl: "",
    imageUrl: "data:image/jpeg;base64,/9j/4AAQSkZJRg==",
    note: "Koton ip ile çok yumuşak oldu!",
    isPublished: true,
    createdAt: new Date(),
  }));

  await assertFails(aliceDb.collection("communityPosts").doc("disc_1").collection("makes").get());
  await assertSucceeds(aliceDb.collection("communityPosts").doc("disc_1").collection("makes").where("isPublished", "==", true).get());

  // Spoofed authorId or cross-user delete fails
  await assertFails(aliceDb.collection("communityPosts").doc("disc_1").collection("makes").doc("mk_spoof").set({
    postId: "disc_1",
    authorId: BOB_UID,
    authorName: "Fake",
    imageUrl: "https://example.com/a.jpg",
    isPublished: true,
    createdAt: new Date(),
  }));
  await assertFails(aliceDb.collection("communityPosts").doc("disc_1").collection("makes").doc("mk_1").delete());
  await assertSucceeds(makeRef.delete());
});

test("User profiles: unauth blocked, owner create/update succeeds, cross-user update fails, post author edit succeeds", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertFails(unauthDb.collection("userProfiles").get());

  const now = new Date();
  const aliceProfileRef = aliceDb.collection("userProfiles").doc(ALICE_UID);
  await assertSucceeds(aliceProfileRef.set({
    userId: ALICE_UID,
    displayName: "Ayşe Örgü",
    username: "@ayseorgu",
    usernameLower: "ayseorgu",
    bio: "Amigurumi ve motif tutkunu",
    specialtyTag: "Amigurumi Ustası",
    photoUrl: "",
    notifyLikes: true,
    notifyComments: true,
    createdAt: now,
    updatedAt: now,
  }));

  await assertSucceeds(bobDb.collection("userProfiles").get());
  await assertSucceeds(aliceProfileRef.update({
    displayName: "Ayşe Yılmaz",
    notifyLikes: false,
    updatedAt: new Date(),
  }));

  // Bob cannot overwrite Alice's profile
  await assertFails(bobDb.collection("userProfiles").doc(ALICE_UID).update({
    displayName: "Hacked",
    updatedAt: new Date(),
  }));

  // Author can edit own communityPost & socialPost
  const postRef = aliceDb.collection("communityPosts").doc("post_edit_1");
  await assertSucceeds(postRef.set(validCommunityPost(ALICE_UID)));
  await assertSucceeds(postRef.update({
    title: "Güncellenmiş Bahar Çantası",
    recipeNote: "Tabanı sık iğne ile güçlendirin.",
    updatedAt: new Date(),
  }));
});

test("Atelier products & hidden markers: admin can create/delete, non-admin denied", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com" }).firestore();
  const adminDb = testEnv.authenticatedContext("admin_uid", { email: "tubisuslu171@gmail.com" }).firestore();

  await assertFails(unauthDb.collection("atelierProducts").where("isPublished", "==", true).get());

  const prodPayload = {
    authorId: "admin_uid",
    title: "El Örmesi Kadife Bebek Battaniyesi",
    subtitle: "Yumuşacık anti-pilling kadife dokulu bebek battaniyesi",
    priceFormatted: "1.450 TL",
    tag: "Yeni Ürün",
    yarnDetail: "Himalaya Dolphin Baby",
    dimensions: "90 x 90 cm",
    imageKey: "img_atelier_shawl",
    imageUrl: "",
    shopierUrl: "https://www.shopier.com/ilmektenisler",
    dolapUrl: "https://dolap.com/profil/ilmektenisler",
    isPublished: true,
    createdAt: new Date(),
    updatedAt: new Date(),
  };

  // Non-admin cannot create
  await assertFails(aliceDb.collection("atelierProducts").doc("ap_1").set({ ...prodPayload, authorId: ALICE_UID }));

  // Admin can create, anyone signed in can list published, unfiltered non-admin fails
  await assertSucceeds(adminDb.collection("atelierProducts").doc("ap_1").set(prodPayload));
  await assertSucceeds(aliceDb.collection("atelierProducts").where("isPublished", "==", true).get());
  await assertFails(aliceDb.collection("atelierProducts").get());

  // Non-admin cannot delete or hide curated product; admin can
  await assertFails(aliceDb.collection("atelierProducts").doc("ap_1").delete());
  await assertFails(aliceDb.collection("atelierHiddenProducts").doc("atelier_cardigan").set({
    productId: "atelier_cardigan",
    hiddenBy: ALICE_UID,
    createdAt: new Date(),
  }));

  await assertSucceeds(adminDb.collection("atelierHiddenProducts").doc("atelier_cardigan").set({
    productId: "atelier_cardigan",
    hiddenBy: "admin_uid",
    createdAt: new Date(),
  }));
  await assertSucceeds(aliceDb.collection("atelierHiddenProducts").get());
  await assertSucceeds(adminDb.collection("atelierProducts").doc("ap_1").delete());
});

