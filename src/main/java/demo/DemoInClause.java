package demo;

import java.sql.*;
import java.util.*;

public class DemoInClause {
    public static void main(String[] args) throws SQLException {
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(new Transaction("1", "CHARGE34268746823743"));
        transactions.add(new Transaction("2", "CHARGE98239028931289"));
        System.out.println(transactions);
        getCharges(transactions);
        System.out.println(transactions);
    }

    public static void getCharges(List<Transaction> transactions) throws SQLException {
        Parameters parameters = getParameters(transactions);

        Map<String, List<String>> chargeTypes = fetchChargeTypes(parameters);

        addChargeTypes(transactions, chargeTypes);
    }

    private static void addChargeTypes(List<Transaction> transactions, Map<String, List<String>> chargeTypes) {
        for (Transaction transaction: transactions) {
            List<String> types = chargeTypes.get(transaction.chargeId);
            if (types != null) {
                transaction.chargeTypes = types;
            }
        }
    }

    private static Map<String, List<String>> fetchChargeTypes(Parameters parameters) throws SQLException {
        Connection connection = DriverManager.getConnection(
                "jdbc:db2://localhost:50000/testdb",
                "db2inst1",
                "Just7Open7It7"
        );
        String sql = String.format("select * from test.charges c where c.charge_id in (%s)", parameters.parameters);
        PreparedStatement statement = connection.prepareStatement(sql);
        int paramIndex = 1;
        for (String parameter: parameters.parameterValues) {
            statement.setString(paramIndex, parameter);
            paramIndex++;
        }
        ResultSet resultSet = statement.executeQuery();
        Map<String, List<String>> chargeTypes = new HashMap<String, List<String>>();
        while (resultSet.next()) {
            String chargeId = resultSet.getString("charge_id");
            String chargeType = resultSet.getString("charge_type");
            List<String> types = chargeTypes.get(chargeId);
            if (types == null) {
                types = new ArrayList<String>();
                types.add(chargeType);
                chargeTypes.put(chargeId, types);
            } else {
                types.add(chargeType);
            }
        }
        System.out.println(chargeTypes);
        statement.close();
        connection.close();
        return chargeTypes;
    }

    private static Parameters getParameters(List<Transaction> transactions) {
        Set<String> chargeIds = new HashSet<String>();
        StringBuilder params = new StringBuilder();
        for (Transaction transaction: transactions) {
            boolean added = chargeIds.add(transaction.chargeId);
            if (added) {
                if (chargeIds.size() > 1) {
                    params.append(',');
                }
                params.append('?');
            }
        }
        System.out.println(params);
        System.out.println(chargeIds);
        return new Parameters(params.toString(), chargeIds);
    }
}

class Transaction {
    public String id;
    public String chargeId;
    public List<String> chargeTypes;

    public Transaction(String id, String chargeId) {
        this.id = id;
        this.chargeId = chargeId;
        this.chargeTypes = new ArrayList<String>();
    }
}

class Parameters {
    public String parameters;
    public Set<String> parameterValues;

    public Parameters(String parameters, Set<String> parameterValues) {
        this.parameters = parameters;
        this.parameterValues = parameterValues;
    }
}
