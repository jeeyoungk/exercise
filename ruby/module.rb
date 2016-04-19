module A
  def self.add(x, y)
    # Ruby does not have module-methods, thus functions need `def` prefix.
    x + y
  end
end

if  __FILE__ == $0
  puts A::add(3, 5)
end
