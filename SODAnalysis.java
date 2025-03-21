import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

class User {
	String userId, name;
	public User(String userId, String name) {
		this.userId = userId;
		this.name = name;
	}
}

class Role {
	String roleId, roleName;
	public Role(String roleId, String roleName) {
		this.roleId = roleId;
		this.roleName = roleName;
	}
}

class Privilege {
	String privilegeId, privilegeName;
	public Privilege(String privilegeId, String privilegeName) {
		this.privilegeId = privilegeId;
		this.privilegeName = privilegeName;
	}
}

class Result {
	String SODId, SODName, userId, userName, roleId, role, accessPoint;

	public Result(String SODId, String SODName, String userId, String userName,
		String roleId, String role, String accessPoint) {
		this.SODId = SODId;
		this.SODName = SODName;
		this.userId = userId;
		this.userName = userName;
		this.roleId = roleId;
		this.role = role;
		this.accessPoint = accessPoint;
	}

	@Override
	public String toString() {
		return "SOD Conflict [" +
		" UserId=" + userId + ", UserName=" + userName +
		", RoleId=" + roleId + ", Role=" + role +
		", accessPoint=" + accessPoint + "]";
	}
}

public class SODAnalysis {
	Map<String, User> users = new HashMap<>();
	Map<String, Role> roles = new HashMap<>();
	Map<String, Privilege> privileges = new HashMap<>();
	Map<String, Set<String>> rolePrivileges = new HashMap<>(); // Role ID -> Privilege IDs
	Map<String, Set<String>> userRoles = new HashMap<>(); // User ID -> Role IDs
	Map<String, String> roleHierarchy = new HashMap<>(); // Role ID -> Parent Role ID
	Set<Set<String>> sodConflicts = new HashSet<>();
	Map<String, String> accessPointEntitlements = new HashMap<>();

	String userFilePath = "data/XX_2_USER_DETAILS_RPT.xlsx";
	String roleFilePath = "data/XX_4_ROLE_MASTER_DETAILS_RPT.xlsx";
	String privsFilePath = "data/XX_7_PVLGS_MASTER_RPT.xlsx";
	String privToRoleFilePath = "data/XX_6_PVLG_TO_ROLE_RELATION_RPT.xlsx";
	String roleToUserFilePath = "data/XX_3_USER_ROLE_MAPPING_RPT.xlsx";
	String sodRulesFilePath = "data/SOD_Ruleset2.xlsx";
	String roleHierarchyFilePath = "data/XX_5_ROLE_TO_ROLE_HIER_RPT.xlsx";
	String SODId;

	public static void main(String[] args) {

		if (args.length != 1) {
			throw new IllegalArgumentException("Missing SOD_ID in command line argument");
		}

		SODAnalysis analysis = new SODAnalysis();
		analysis.SODId = args[0];
		analysis.setup();
		analysis.analyzeSODConflicts();
	}

	public void setup() {
		try {
			readUsers();
			readRoles();
			readPrivileges();
			readPrivilegeToRoleMappings();
			readRoleToUserMappings();
			readRoleHierarchy();
			readSODRules();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readUsers() throws IOException {
		Sheet sheet = readExcelFile(userFilePath);
		for (Row row : sheet) {
			if (row.getRowNum() == 0) continue;
			String userId = getCellValue(row.getCell(5));
			String userName = getCellValue(row.getCell(2));
			users.put(userId, new User(userId, userName));
		}
	}

	private void readRoles() throws IOException {
		Sheet sheet = readExcelFile(roleFilePath);
		for (Row row : sheet) {
			if (row.getRowNum() == 0) continue;
			String roleId = getCellValue(row.getCell(0));
			String roleName = getCellValue(row.getCell(1));
			roles.put(roleId, new Role(roleId, roleName));
		}
	}

	private void readPrivileges() throws IOException {
		Sheet sheet = readExcelFile(privsFilePath);
		for (Row row : sheet) {
			if (row.getRowNum() == 0) continue;
			String privId = getCellValue(row.getCell(2));
			String privName = getCellValue(row.getCell(1));
			privileges.put(privId, new Privilege(privId, privName));
		}
	}

	private void readPrivilegeToRoleMappings() throws IOException {
		Sheet sheet = readExcelFile(privToRoleFilePath);
		for (Row row : sheet) {
			if (row.getRowNum() == 0) continue;
			String roleId = getCellValue(row.getCell(2));
			String privId = getCellValue(row.getCell(1));
			rolePrivileges.computeIfAbsent(roleId, k -> new HashSet<>()).add(privId);
		}
	}

	private void readRoleToUserMappings() throws IOException {
		Sheet sheet = readExcelFile(roleToUserFilePath);
		for (Row row : sheet) {
			if (row.getRowNum() == 0) continue;
			String userId = getCellValue(row.getCell(2));
			String roleId = getCellValue(row.getCell(0));
			userRoles.computeIfAbsent(userId, k -> new HashSet<>()).add(roleId);
		}
	}

	private void readRoleHierarchy() throws IOException {
		Sheet sheet = readExcelFile(roleHierarchyFilePath);
		for (Row row : sheet) {
			if (row.getRowNum() == 0) continue;
			String childRoleId = getCellValue(row.getCell(1));
			String parentRoleId = getCellValue(row.getCell(2));
			roleHierarchy.put(childRoleId, parentRoleId);
		}
	}

	private void readSODRules() throws IOException {
		Workbook workbook = readExcelFile2(sodRulesFilePath);
		Sheet sheet = workbook.getSheetAt(0);
		for (Row row : sheet) {
			if (row.getRowNum() == 0) continue;
			String sodId = getCellValue(row.getCell(0));

			// Read only asked SOD Rule
			if (sodId.trim().equals(this.SODId)) {
				String leg1 = getCellValue(row.getCell(1));
				String leg2 = getCellValue(row.getCell(2));
				if (leg1.isEmpty() || leg2.isEmpty()) continue;
				sodConflicts.add(new HashSet<>(Arrays.asList(leg1, leg2)));
				break;
			}

		}
		sheet = workbook.getSheetAt(1);
		int index = 0;
		for (Row row : sheet) {
			++index;
			if (row.getRowNum() == 0) continue;
			String entitlement = getCellValue(row.getCell(0));
			String accessPoint = getCellValue(row.getCell(1));
			if (entitlement.isEmpty() || accessPoint.isEmpty()) continue;
			accessPointEntitlements.put(accessPoint, entitlement);
		}
	}

	private Set<String> getAllRoles(String roleId) {
		Set<String> allRoles = new HashSet<>();
		while (roleId != null) {
			allRoles.add(roleId);
			roleId = roleHierarchy.get(roleId);
		}
		return allRoles;
	}

	public void analyzeSODConflicts() {

		List<Result> results = new ArrayList<>();

		for (String userId : userRoles.keySet()) {
			Set<String> userPrivileges = new HashSet<>();

			// NOTE Get all roles assigned to the user, including inherited ones
			Set<String> allUserRoles = new HashSet<>();
			for (String roleId : userRoles.get(userId)) {
				allUserRoles.addAll(getAllRoles(roleId));
			}

			// Collect privileges from all roles
			for (String roleId : allUserRoles) {
				Set<String> privilegeIds = rolePrivileges.getOrDefault(roleId, Collections.emptySet());
				for (String id : privilegeIds) {
					Privilege priv = privileges.get(id);
					if (priv != null) {
						userPrivileges.add(priv.privilegeName);
					}
				}
			}

			Set<String> entitlements = new HashSet<>();
			for (String priv : userPrivileges) {
				String entitlement = accessPointEntitlements.get(priv);
				if (entitlement != null) {
					entitlements.add(entitlement);
				}
			}

			for (Set<String> conflictSet : sodConflicts) {
				if (entitlements.containsAll(conflictSet)) {
					// System.out.println("User ID: " + userId);
					// System.out.println(entitlements);
					// System.out.println("========================");
					User user = users.get(userId);
					if (user != null) {
						for (String roleId : allUserRoles) {
							Role role = roles.get(roleId);
							if (role != null) {
								for (String priv : userPrivileges) {
									Result result = new Result("", "",
										userId, user.name,
										roleId, role.roleName,
										priv);
									results.add(result);
								}
							}
						}
						// System.out.println("SOD Conflict detected for user: " + user.name);
						break;
					}
				}
			}
		}

		for (Result r : results) {
			System.out.println(r);
		}
	}

	private static Sheet readExcelFile(String filePath) throws IOException {
		try (FileInputStream fis = new FileInputStream(new File(filePath));
			Workbook workbook = new XSSFWorkbook(fis)) {
			return workbook.getSheetAt(0);
		}
	}

	private static Workbook readExcelFile2(String filePath) throws IOException {
		try (FileInputStream fis = new FileInputStream(new File(filePath));
			Workbook workbook = new XSSFWorkbook(fis)) {
			return workbook;
		}
	}

	private static String getCellValue(Cell cell) {
		if (cell == null) return "";
			switch (cell.getCellType()) {
			case STRING: return cell.getStringCellValue();
			case NUMERIC: return new BigDecimal(cell.getNumericCellValue()).toPlainString();
			case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
			case FORMULA: return cell.getCellFormula();
			default: return "";
		}
	}
}
