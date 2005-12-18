package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.Tree.*;
import net.sourceforge.mayfly.parser.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class CreateSchema extends Command {

    private final String schemaName;
    private L createTableCommands;

    public static CreateSchema createSchemaFromTree(Tree tree) {
        Children children = tree.children();
        Tree schemaName = (Tree) children.element(0);
        CreateSchema schema = new CreateSchema(schemaName.getText());
        for (int i = 1; i < children.size(); i++) {
            Tree createTable = (Tree) children.element(i);
            if (createTable.getType() != SQLTokenTypes.CREATE_TABLE) {
                throw new MayflyInternalException(
                    "Didn't expect create schema to have " + createTable.toStringTree());
            }
            CreateTable createTableCommand = CreateTable.createTableFromTree(createTable);
            schema.add(createTableCommand);
        }
        return schema;
    }

    public CreateSchema(String schemaName) {
        this.schemaName = schemaName;
        createTableCommands = new L();
    }

    private void add(CreateTable command) {
        createTableCommands.add(command);
    }

    public void substitute(Collection jdbcParameters) {
        throw new UnimplementedException();
    }

    public int parameterCount() {
        throw new UnimplementedException();
    }

    public DataStore update(DataStore store, String currentSchema) {
        Schema schema = new Schema();
        for (Iterator iter = createTableCommands.iterator(); iter.hasNext();) {
            CreateTable command = (CreateTable) iter.next();
            schema = command.update(schema);
        }
        return store.with(schemaName, schema);
    }

    public int rowsAffected() {
        return 0;
    }

}
