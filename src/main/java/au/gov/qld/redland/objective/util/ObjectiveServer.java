package au.gov.qld.redland.objective.util;

public enum ObjectiveServer {DEV("DEV"), TEST("TEST"), UAT("UAT"), PRD("PRD");
  private String name;

  private ObjectiveServer(String name) {
      this.name = name;
  }
  
  @Override
  public String toString() {
      return name;
  }

}