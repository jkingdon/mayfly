package net.sourceforge.mayfly.jdbc;

import net.sourceforge.mayfly.MayflyConnection;
import net.sourceforge.mayfly.UnimplementedException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcMetaData implements DatabaseMetaData {

    private final MayflyConnection mayflyConnection;

    public JdbcMetaData(MayflyConnection mayflyConnection) {
        this.mayflyConnection = mayflyConnection;
    }

    public boolean allProceduresAreCallable() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean allTablesAreSelectable() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean deletesAreDetected(int arg0) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getAttributes(String arg0, String arg1, String arg2, String arg3) throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getBestRowIdentifier(String arg0, String arg1, String arg2, int arg3, boolean arg4) throws SQLException {
        throw new UnimplementedException();
    }

    public String getCatalogSeparator() throws SQLException {
        throw new UnimplementedException();
    }

    public String getCatalogTerm() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getCatalogs() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getColumnPrivileges(String arg0, String arg1, String arg2, String arg3) throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getColumns(
        String catalog, String schemaPattern, String table, String column) 
    throws SQLException {
        return mayflyConnection.getColumns(table, column);
    }

    public Connection getConnection() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getCrossReference(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5)
        throws SQLException {
        throw new UnimplementedException();
    }

    public int getDatabaseMajorVersion() throws SQLException {
        throw new UnimplementedException();
    }

    public int getDatabaseMinorVersion() throws SQLException {
        throw new UnimplementedException();
    }

    public String getDatabaseProductName() throws SQLException {
        return "Mayfly";
    }

    public String getDatabaseProductVersion() throws SQLException {
        throw new UnimplementedException();
    }

    public int getDefaultTransactionIsolation() throws SQLException {
        throw new UnimplementedException();
    }

    public int getDriverMajorVersion() {
        throw new UnimplementedException();
    }

    public int getDriverMinorVersion() {
        throw new UnimplementedException();
    }

    public String getDriverName() throws SQLException {
        throw new UnimplementedException();
    }

    public String getDriverVersion() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getExportedKeys(String arg0, String arg1, String arg2) throws SQLException {
        throw new UnimplementedException();
    }

    public String getExtraNameCharacters() throws SQLException {
        throw new UnimplementedException();
    }

    public String getIdentifierQuoteString() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getImportedKeys(String arg0, String arg1, String arg2) throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getIndexInfo(String arg0, String arg1, String arg2, boolean arg3, boolean arg4) throws SQLException {
        throw new UnimplementedException();
    }

    public int getJDBCMajorVersion() throws SQLException {
        throw new UnimplementedException();
    }

    public int getJDBCMinorVersion() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxBinaryLiteralLength() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxCatalogNameLength() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxCharLiteralLength() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxColumnNameLength() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxColumnsInGroupBy() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxColumnsInIndex() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxColumnsInOrderBy() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxColumnsInSelect() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxColumnsInTable() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxConnections() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxCursorNameLength() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxIndexLength() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxProcedureNameLength() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxRowSize() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxSchemaNameLength() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxStatementLength() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxStatements() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxTableNameLength() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxTablesInSelect() throws SQLException {
        throw new UnimplementedException();
    }

    public int getMaxUserNameLength() throws SQLException {
        throw new UnimplementedException();
    }

    public String getNumericFunctions() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getPrimaryKeys(String arg0, String arg1, String arg2) throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getProcedureColumns(String arg0, String arg1, String arg2, String arg3) throws SQLException {
        throw new UnimplementedException();
    }

    public String getProcedureTerm() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getProcedures(String arg0, String arg1, String arg2) throws SQLException {
        throw new UnimplementedException();
    }

    public int getResultSetHoldability() throws SQLException {
        throw new UnimplementedException();
    }

    public String getSQLKeywords() throws SQLException {
        throw new UnimplementedException();
    }

    public int getSQLStateType() throws SQLException {
        throw new UnimplementedException();
    }

    public String getSchemaTerm() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getSchemas() throws SQLException {
        throw new UnimplementedException();
    }

    public String getSearchStringEscape() throws SQLException {
        throw new UnimplementedException();
    }

    public String getStringFunctions() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getSuperTables(String arg0, String arg1, String arg2) throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getSuperTypes(String arg0, String arg1, String arg2) throws SQLException {
        throw new UnimplementedException();
    }

    public String getSystemFunctions() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getTablePrivileges(String arg0, String arg1, String arg2) throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getTableTypes() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getTables(String arg0, String arg1, String arg2, String[] arg3) throws SQLException {
        throw new UnimplementedException();
    }

    public String getTimeDateFunctions() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getTypeInfo() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getUDTs(String arg0, String arg1, String arg2, int[] arg3) throws SQLException {
        throw new UnimplementedException();
    }

    public String getURL() throws SQLException {
        throw new UnimplementedException();
    }

    public String getUserName() throws SQLException {
        throw new UnimplementedException();
    }

    public ResultSet getVersionColumns(String arg0, String arg1, String arg2) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean insertsAreDetected(int arg0) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean isCatalogAtStart() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean isReadOnly() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean locatorsUpdateCopy() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean nullPlusNonNullIsNull() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean nullsAreSortedAtEnd() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean nullsAreSortedAtStart() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean nullsAreSortedHigh() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean nullsAreSortedLow() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean othersDeletesAreVisible(int arg0) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean othersInsertsAreVisible(int arg0) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean othersUpdatesAreVisible(int arg0) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean ownDeletesAreVisible(int arg0) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean ownInsertsAreVisible(int arg0) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean ownUpdatesAreVisible(int arg0) throws SQLException {
        throw new UnimplementedException();
    }

    public boolean storesLowerCaseIdentifiers() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean storesMixedCaseIdentifiers() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean storesUpperCaseIdentifiers() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        throw new UnimplementedException();
    }

    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return false;
    }

    public boolean supportsANSI92FullSQL() throws SQLException {
        return false;
    }

    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return false;
    }

    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return true;
    }

    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return true;
    }

    public boolean supportsBatchUpdates() throws SQLException {
        return false;
    }

    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return false;
    }

    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return false;
    }

    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return false;
    }

    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return false;
    }

    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return false;
    }

    public boolean supportsColumnAliasing() throws SQLException {
        return false;
    }

    public boolean supportsConvert() throws SQLException {
        return false;
    }

    public boolean supportsConvert(int arg0, int arg1) throws SQLException {
        return false;
    }

    public boolean supportsCoreSQLGrammar() throws SQLException {
        return false;
    }

    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return true;
    }

    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return false;
    }

    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return false;
    }

    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return false;
    }

    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return false;
    }

    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return false;
    }

    public boolean supportsFullOuterJoins() throws SQLException {
        return false;
    }

    public boolean supportsGetGeneratedKeys() throws SQLException {
        return false;
    }

    public boolean supportsGroupBy() throws SQLException {
        return true;
    }

    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return false;
    }

    public boolean supportsGroupByUnrelated() throws SQLException {
        return false;
    }

    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return false;
    }

    public boolean supportsLikeEscapeClause() throws SQLException {
        return false;
    }

    public boolean supportsLimitedOuterJoins() throws SQLException {
        return false;
    }

    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return false;
    }

    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return false;
    }

    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return false;
    }

    public boolean supportsMultipleOpenResults() throws SQLException {
        return false;
    }

    public boolean supportsMultipleResultSets() throws SQLException {
        return false;
    }

    public boolean supportsMultipleTransactions() throws SQLException {
        return false;
    }

    public boolean supportsNamedParameters() throws SQLException {
        return false;
    }

    public boolean supportsNonNullableColumns() throws SQLException {
        return true;
    }

    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return false;
    }

    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return false;
    }

    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return false;
    }

    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return false;
    }

    public boolean supportsOrderByUnrelated() throws SQLException {
        return false;
    }

    public boolean supportsOuterJoins() throws SQLException {
        // no right join
        return false;
    }

    public boolean supportsPositionedDelete() throws SQLException {
        return false;
    }

    public boolean supportsPositionedUpdate() throws SQLException {
        return false;
    }

    public boolean supportsResultSetConcurrency(int arg0, int arg1) 
    throws SQLException {
        // I think this is true; need to check what it means.
        return false;
    }

    public boolean supportsResultSetHoldability(int arg0) throws SQLException {
        return false;
    }

    public boolean supportsResultSetType(int arg0) throws SQLException {
        return false;
    }

    public boolean supportsSavepoints() throws SQLException {
        return false;
    }

    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return false;
    }

    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return false;
    }

    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return false;
    }

    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return false;
    }

    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return false;
    }

    public boolean supportsSelectForUpdate() throws SQLException {
        return false;
    }

    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }

    public boolean supportsStoredProcedures() throws SQLException {
        return false;
    }

    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return false;
    }

    public boolean supportsSubqueriesInExists() throws SQLException {
        return false;
    }

    public boolean supportsSubqueriesInIns() throws SQLException {
        return false;
    }

    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return false;
    }

    public boolean supportsTableCorrelationNames() throws SQLException {
        return false;
    }

    public boolean supportsTransactionIsolationLevel(int arg0) throws SQLException {
        return false;
    }

    public boolean supportsTransactions() throws SQLException {
        return false;
    }

    public boolean supportsUnion() throws SQLException {
        return false;
    }

    public boolean supportsUnionAll() throws SQLException {
        return false;
    }

    public boolean updatesAreDetected(int arg0) throws SQLException {
        return false;
    }

    public boolean usesLocalFilePerTable() throws SQLException {
        return false;
    }

    public boolean usesLocalFiles() throws SQLException {
        return false;
    }

}
