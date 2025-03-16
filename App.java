import java.util.*;

class User {
    String userId;
    String name;

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }
}

class Role {
    String roleId;
    String roleName;

    public Role(String roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }
}

class Privilege {
    String privilegeId;
    String privilegeName;

    public Privilege(String privilegeId, String privilegeName) {
        this.privilegeId = privilegeId;
        this.privilegeName = privilegeName;
    }
}

class Entitlement {
    String entitlementId;
    Set<String> privileges;

    public Entitlement(String entitlementId, Set<String> privileges) {
        this.entitlementId = entitlementId;
        this.privileges = privileges;
    }
}

public class SODAnalysis {

    Map<String, User> users = new HashMap<>();
    Map<String, Role> roles = new HashMap<>();
    Map<String, Privilege> privileges = new HashMap<>();
    Map<String, Set<String>> rolePrivileges = new HashMap<>(); // Role ID -> Set of Privilege IDs
    Map<String, Set<String>> userRoles = new HashMap<>(); // User ID -> Set of Role IDs

    // SOD Conflict rules (entitlement pairs)
    Set<Set<String>> sodConflicts = new HashSet<>(Arrays.asList(
        new HashSet<>(Arrays.asList("Create_Payables_Invoices", "Approve_Payables_Invoices"))
    ));

    public void addUser(String userId, String name) {
        users.put(userId, new User(userId, name));
    }

    public void addRole(String roleId, String roleName) {
        roles.put(roleId, new Role(roleId, roleName));
    }

    public void addPrivilege(String privilegeId, String privilegeName) {
        privileges.put(privilegeId, new Privilege(privilegeId, privilegeName));
    }

    public void assignPrivilegeToRole(String roleId, String privilegeId) {
        rolePrivileges.computeIfAbsent(roleId, k -> new HashSet<>()).add(privilegeId);
    }

    public void assignRoleToUser(String userId, String roleId) {
        userRoles.computeIfAbsent(userId, k -> new HashSet<>()).add(roleId);
    }

    public void analyzeSODConflicts() {
        for (String userId : userRoles.keySet()) {
            Set<String> userPrivileges = new HashSet<>();
            for (String roleId : userRoles.get(userId)) {
                userPrivileges.addAll(rolePrivileges.getOrDefault(roleId, Collections.emptySet()));
            }
            for (Set<String> conflictSet : sodConflicts) {
                if (userPrivileges.containsAll(conflictSet)) {
                    System.out.println("SOD Conflict detected for user: " + users.get(userId).name);
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        SODAnalysis analysis = new SODAnalysis();

        // Users
        analysis.addUser("U1", "John Doe");

        // Roles
        analysis.addRole("R1", "Accounts Payable Specialist");
        analysis.addRole("R2", "Payment Approval Duty");

        // Privileges
        analysis.addPrivilege("P1", "Create Payables Invoices");
        analysis.addPrivilege("P2", "Approve Payables Invoices");

        // Assign Privileges to Roles
        analysis.assignPrivilegeToRole("R1", "P1");
        analysis.assignPrivilegeToRole("R2", "P2");

        // Assign Roles to User
        analysis.assignRoleToUser("U1", "R1");
        analysis.assignRoleToUser("U1", "R2");

        // Analyze SOD Conflicts
        analysis.analyzeSODConflicts();
    }
}
