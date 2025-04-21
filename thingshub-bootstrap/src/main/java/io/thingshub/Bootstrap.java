package io.thingshub;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.security.CodeSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Bootstrap
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
public class Bootstrap {

//	private static final String PROPERTIES_SYMBOL = ".properties";
//
//	private static final String YAML_SYMBOL_1 = ".yaml";
//
//	private static final String YAML_SYMBOL_2 = ".yml";

	private static final PosixFilePermission[] WRITE_PERMISSIONS = { //
			PosixFilePermission.OWNER_WRITE, //
			PosixFilePermission.GROUP_WRITE, //
			PosixFilePermission.OTHERS_WRITE };

	public static void main(String[] args) throws Exception {
		Map<String, Object> configs = Maps.newHashMap();

		CodeSource codeSource = Bootstrap.class.getProtectionDomain().getCodeSource();
		if (codeSource != null) {
			File parentFile = new File(codeSource.getLocation().getPath()).getParentFile();
			File confPath = new File(parentFile.getParent() + "/conf");
			if (!confPath.exists()) {
				confPath = new File(parentFile.getParentFile().getParent() + "/conf");
			}

			File bannerFile = new File(confPath + "/banner.txt");
			if (bannerFile.exists()) {
				printBanner(bannerFile);
			}

			File configFile = new File(confPath + "/config.yaml");
			if (configFile.exists()) {
				InputStream in = new FileInputStream(configFile);

				try {
					Constructor c = new Constructor(new LoaderOptions());
					c.setPropertyUtils(new PropertyUtils() {
						@Override
						public Property getProperty(Class<? extends Object> type, String name) {
							if (name.indexOf('-') > -1) {
								name = YmlCamelCase.camelize(name);
							}
							return super.getProperty(type, name);
						}
					});
					Yaml yaml = new Yaml(c);
					Map<String, Object> items = yaml.load(in);
					String rootKey = "";
					extractConfigItems(items, rootKey, configs);
				} catch (Exception e) {
					log.error("Failed to read config.yaml", e);
					throw new BootException("Failed to read config.yaml. Error: ", e);
				} finally {
					if (in != null) {
						in.close();
					}
				}
			}
		}

		Broker.builder().configs(configs).build().startup().doOnSuccess(b -> writePid()).block();
	}

	@SuppressWarnings("unchecked")
	private static void extractConfigItems(Map<String, Object> items, String parentKey, Map<String, Object> configs) {
		items.entrySet().forEach(entry -> {
			if (entry.getValue() instanceof Map) {
				LinkedHashMap<String, Object> child = (LinkedHashMap<String, Object>) entry.getValue();
				extractConfigItems(child, parentKey + (parentKey.equals("") ? "" : ".") + entry.getKey(), configs);
			} else {
				// visitor
				configs.put(parentKey + (parentKey.equals("") ? "" : ".") + entry.getKey(), entry.getValue());
			}
		});
	}

	private static void writePid() {
		String jvmName = ManagementFactory.getRuntimeMXBean().getName();
		String pid = jvmName.split("@")[0];

		CodeSource codeSource = Bootstrap.class.getProtectionDomain().getCodeSource();
		if (codeSource != null) {
			try {
				File parentFile = new File(codeSource.getLocation().getPath()).getParentFile();

				File binPath = new File(parentFile.getParent() + "/bin");
				if (!binPath.exists()) {
					binPath = new File(parentFile.getParentFile().getParent() + "/bin");
					binPath.mkdirs();
				}

				File pidFile = new File(binPath + "/thingshub.pid");
				if (pidFile.exists()) {
					pidFile.delete();
//					if (!pidFile.canWrite() || !canWritePosixFile(pidFile)) {
//						throw new FileNotFoundException(pidFile.toString() + " (permission denied)");
//					}
				}

				try (FileWriter writer = new FileWriter(pidFile)) {
					writer.append(pid);
				}

				pidFile.deleteOnExit();
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	private static boolean canWritePosixFile(File file) throws IOException {
		try {
			Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(file.toPath());
			for (PosixFilePermission permission : WRITE_PERMISSIONS) {
				if (permissions.contains(permission)) {
					return true;
				}
			}
			return false;
		} catch (UnsupportedOperationException ex) {
			return true;
		}
	}

	public static void printBanner(File bannerFile) throws IOException {
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(bannerFile));

			String lines = "\n", line = "";
			while ((line = br.readLine()) != null) {
				lines += (line + "\n");
			}

			System.out.println(lines);
		} catch (Exception e) {
			log.error("Failed to read banner", e);
			throw new BootException("Failed to read banner. Error: ", e);
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

}