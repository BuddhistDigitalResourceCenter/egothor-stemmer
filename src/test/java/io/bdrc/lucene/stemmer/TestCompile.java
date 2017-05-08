package io.bdrc.lucene.stemmer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.StringTokenizer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestCompile {

	@Rule
    public TemporaryFolder tmpFolder= new TemporaryFolder();
	
	@Test
	public void testCompile() throws Exception {
		File tmpDir = tmpFolder.newFolder("testCompile");
		Path dir = tmpDir.toPath();
		Path output = dir.resolve("testRules.txt");
		try (InputStream input = getClass().getResourceAsStream("/testRules.txt")) {
			Files.copy(input, output);
		}
		String path = output.toAbsolutePath().toString();
		Compile.main(new String[] {"test", path});
		Path compiled = dir.resolve("testRules.txt.out");
		Trie trie = loadTrie(compiled);
		assertTrie(trie, output, true, true);
		assertTrie(trie, output, false, true);
	}

	@Test
	public void testCompileBackwards() throws Exception {
		File tmpDir = tmpFolder.newFolder("testCompile");
		Path dir = tmpDir.toPath();
		Path output = dir.resolve("testRules.txt");
		try (InputStream input = getClass().getResourceAsStream("/testRules.txt")) {
			Files.copy(input, output);
		}
		String path = output.toAbsolutePath().toString();
		Compile.main(new String[] {"-test", path});
		Path compiled = dir.resolve("testRules.txt.out");
		Trie trie = loadTrie(compiled);
		assertTrie(trie, output, true, true);
		assertTrie(trie, output, false, true);
	}

	@Test
	public void testCompileMulti() throws Exception {
		File tmpDir = tmpFolder.newFolder("testCompile");
		Path dir = tmpDir.toPath();
		Path output = dir.resolve("testRules.txt");
		try (InputStream input = getClass().getResourceAsStream("/testRules.txt")) {
			Files.copy(input, output);
		}
		String path = output.toAbsolutePath().toString();
		Compile.main(new String[] {"Mtest", path});
		Path compiled = dir.resolve("testRules.txt.out");
		Trie trie = loadTrie(compiled);
		assertTrie(trie, output, true, true);
		assertTrie(trie, output, false, true);
	}

	static Trie loadTrie(Path path) throws IOException {
		Trie trie;
		DataInputStream is = new DataInputStream(new BufferedInputStream(
				Files.newInputStream(path)));
		String method = is.readUTF().toUpperCase(Locale.ROOT);
		if (method.indexOf('M') < 0) {
			trie = new Trie(is);
		} else {
			trie = new MultiTrie(is);
		}
		is.close();
		return trie;
	}

	private static void assertTrie(Trie trie, Path file, boolean usefull,
			boolean storeorig) throws Exception {
		LineNumberReader in = new LineNumberReader(Files.newBufferedReader(file, StandardCharsets.UTF_8));

		for (String line = in.readLine(); line != null; line = in.readLine()) {
			try {
				line = line.toLowerCase(Locale.ROOT);
				StringTokenizer st = new StringTokenizer(line);
				String stem = st.nextToken();
				if (storeorig) {
					CharSequence cmd = (usefull) ? trie.getFully(stem) : trie
							.getLastOnPath(stem);
					StringBuilder stm = new StringBuilder(stem);
					Diff.apply(stm, cmd);
					assert(stem.toLowerCase(Locale.ROOT).equals(stm.toString().toLowerCase(Locale.ROOT)));
				}
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					if (token.equals(stem)) {
						continue;
					}
					CharSequence cmd = (usefull) ? trie.getFully(token) : trie
							.getLastOnPath(token);
					StringBuilder stm = new StringBuilder(token);
					Diff.apply(stm, cmd);
					assert(stem.toLowerCase(Locale.ROOT).equals(stm.toString().toLowerCase(Locale.ROOT)));
				}
			} catch (java.util.NoSuchElementException x) {
				// no base token (stem) on a line
			}
		}

		in.close();
	}
}