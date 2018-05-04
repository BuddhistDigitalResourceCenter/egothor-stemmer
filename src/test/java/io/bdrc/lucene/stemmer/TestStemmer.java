package io.bdrc.lucene.stemmer;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class TestStemmer {

	@Test
	public void testTrie() {
		Trie t = new Trie(true);

		String keys[] = {"a", "ba", "bb", "c"};
		String vals[] = {"1", "2", "2", "4"};

		for (int i = 0; i < keys.length; i++) {
			t.add(keys[i], vals[i]);
		}

		assert(0 == t.root);
		assert(2 == t.rows.size());
		assert(3 == t.cmds.size());   
		assertTrieContents(t, keys, vals);
	}

	@Test
	public void testTrieBackwards() {
		Trie t = new Trie(false);

		String keys[] = {"a", "ba", "bb", "c"};
		String vals[] = {"1", "2", "2", "4"};

		for (int i = 0; i < keys.length; i++) {
			t.add(keys[i], vals[i]);
		}

		assertTrieContents(t, keys, vals);
	}

    @Test
    public void optimizationTest() throws IOException
    {
		Trie t = new Trie(true);

		String keys[] = {"tat", "tattvopaplavasiMha", "tattvopaplavasiMhatas", "tattvopaplavasiMhataH"};
		String vals[] = {"1", "2", "3", "4"};

		for (int i = 0; i < keys.length; i++) {
			t.add(keys[i], vals[i]);
		}
		t = t.reduce(new Reduce());
		assertTrue(t.getFully("tattva") == null);
    }

    @Test
    public void optimizationTest2() throws IOException
    {
		Trie t = new Trie(true);

		String keys[] = {"tattva", "sattva"};
		String vals[] = {"1", "1"};

		for (int i = 0; i < keys.length; i++) {
			t.add(keys[i], vals[i]);
		}
		t = t.reduce(new Reduce());
		assertTrue(t.getFully("tattva") == "1");
    }
	
	private static void assertTrieContents(Trie trie, String keys[], String vals[]) {
		Trie[] tries = new Trie[] {
				trie,
				trie.reduce(new Reduce()),
		};

		for (Trie t : tries) {
			for (int i = 0; i < keys.length; i++) {
				assertTrue(vals[i].equals(t.getFully(keys[i]).toString()));
				assertTrue(vals[i].equals(t.getLastOnPath(keys[i]).toString()));
			}
		}
	}
}
